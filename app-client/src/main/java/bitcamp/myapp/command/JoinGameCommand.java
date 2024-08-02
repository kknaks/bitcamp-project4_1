package bitcamp.myapp.command;

import bitcamp.command.Command;
import bitcamp.context.ApplicationContext;
import bitcamp.util.Prompt;
import bitcamp.vo.Room;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class JoinGameCommand implements Command {
  ApplicationContext appCtx;

  public JoinGameCommand(ApplicationContext appCtx) {
    this.appCtx = appCtx;
  }

  @Override
  public void execute(String menuName) {
    try (Socket socket = new Socket("localhost", 8888);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
      System.out.println("서버와 연결 되었습니다.");

      // 서버에 방 목록 요청
      out.writeObject("GET_ROOMS");
      out.flush();

      // 방 목록 수신
      List<Room> rooms = (List<Room>) in.readObject();
      if (rooms.isEmpty()) {
        System.out.println("현재 생성된 방이 없습니다.");
        return;
      }

      // 방 목록 출력
      System.out.println("현재 생성된 방 목록:");
      for (Room room : rooms) {
        System.out.printf("방 번호: %d, 방 제목: %s, 포트: %d\n", room.getNo(), room.getTitle(),
            room.getPort());
      }

      // 사용자로부터 방 번호 입력받기
      int roomNo = Prompt.inputInt("참여할 방 번호를 입력하세요: ");
      Room selectedRoom =
          rooms.stream().filter(room -> room.getNo() == roomNo).findFirst().orElse(null);

      if (selectedRoom == null) {
        System.out.println("해당 번호의 방이 없습니다.");
        return;
      }

      // 선택한 방의 포트로 서버 연결
      MultiGameCommand multiGameCommand = new MultiGameCommand(appCtx, selectedRoom);
      multiGameCommand.execute(menuName);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
