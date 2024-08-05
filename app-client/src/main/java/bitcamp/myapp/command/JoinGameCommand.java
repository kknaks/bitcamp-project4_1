package bitcamp.myapp.command;

import bitcamp.command.Command;
import bitcamp.context.ApplicationContext;
import bitcamp.util.Prompt;
import bitcamp.vo.Room;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import static bitcamp.net.ResponseStatus.MAIN_SERVER_IP;
import static bitcamp.net.ResponseStatus.MAIN_SERVER_PORT;

public class JoinGameCommand implements Command {
  ApplicationContext appCtx;

  public JoinGameCommand(ApplicationContext appCtx) {
    this.appCtx = appCtx;
  }

  private static int getAdjustedWidth(String s, int width) {
    int length = s.length();
    int nonAsciiCount = 0;

    for (char c : s.toCharArray()) {
      if (c > 127) {
        nonAsciiCount++;
      }
    }
    return width - ((2 * nonAsciiCount - 1) / 2);
  }

  @Override
  public void execute(String menuName) {
    try (Socket socket = new Socket(MAIN_SERVER_IP, MAIN_SERVER_PORT);
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
        System.out.println("| 방번호 | 방제목 |  사이즈  |  승리돌  | 현재원 |");
        System.out.printf("|%6d  |", room.getNo() + 1);
        System.out.printf("%" + getAdjustedWidth(room.getTitle(), 7) + "s|", room.getTitle());
        System.out.printf(" %3s*%3s  | %6d   | %4d/2 |\n", room.getSize(), room.getSize(),
            room.getCount(), room.getMemberCount());
      }

      // 사용자로부터 방 번호 입력받기
      int roomNo = Prompt.inputInt("참여할 방 번호를 입력하세요(이전 :0): ") - 1;
      if (roomNo == -1) {
        return;
      }

      Room selectedRoom =
          rooms.stream().filter(room -> room.getNo() == roomNo).findFirst().orElse(null);

      if (selectedRoom == null && roomNo != -1) {
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
