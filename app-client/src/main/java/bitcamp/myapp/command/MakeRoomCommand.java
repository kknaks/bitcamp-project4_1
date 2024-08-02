package bitcamp.myapp.command;

import bitcamp.command.Command;
import bitcamp.context.ApplicationContext;
import bitcamp.util.Prompt;
import bitcamp.vo.Room;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

public class MakeRoomCommand implements Command {
  ApplicationContext appCtx;

  public MakeRoomCommand(ApplicationContext appCtx) {
    this.appCtx = appCtx;
  }

  public void execute(String menuName) {
    try (Socket socket = new Socket("localhost", 8888);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
      System.out.println("서버와 연결 되었습니다.");

      out.writeObject("CREATE_ROOM");
      out.flush();

      Room room = new Room();
      room.setNo(Room.setSeqRoomNo());
      room.setPort(Room.setSeqPort());
      room.setTitle(Prompt.input("방제목: "));
      room.setSize(Prompt.inputInt("배열 수: "));
      room.setCount(Prompt.inputInt("승리 돌 수: "));
      out.writeObject(room);

      System.out.println("방 생성 완료. 게임을 시작합니다.");
      System.out.println(room.getPort());
      MultiGameCommand multiGameCommand = new MultiGameCommand(appCtx, room);
      multiGameCommand.execute(menuName);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
