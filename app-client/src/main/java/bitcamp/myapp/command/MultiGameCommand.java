package bitcamp.myapp.command;

import bitcamp.command.Command;
import bitcamp.context.ApplicationContext;
import bitcamp.util.Prompt;
import bitcamp.vo.Room;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static bitcamp.net.ResponseStatus.MAIN_SERVER_IP;

public class MultiGameCommand implements Command {
  ApplicationContext appCtx;
  Room room;
  String[][] array;

  public MultiGameCommand(ApplicationContext appCtx, Room room) {
    this.appCtx = appCtx;
    this.room = room;
  }

  public static Integer parseIntOrNull(String s) {
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  @Override
  public void execute(String menuName) {
    try {
      Thread.sleep(2000); // 2초 대기

      try (Socket socket = new Socket(MAIN_SERVER_IP, room.getPort());
          ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
          ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
        System.out.println("방에 입장 하였습니다.");
        System.out.println("대기중.....");

        while (true) {
          String message = (String) in.readObject();
          System.out.println(message);

          if (message.equals("게임 종료")) {
            System.out.println("게임이 종료되었습니다.");
            break;
          }

          if (message.contains("배열")) {
            String[][] arr = (String[][]) in.readObject();

            array = arr;

            for (int i = 0; i < arr.length; i++) {
              for (int j = 0; j < arr[i].length; j++) {
                System.out.printf("%s|", arr[i][j]);
              }
              System.out.println();
              for (int j = 0; j < arr.length * 4; j++) {
                System.out.print("-");
              }
              System.out.println();
            }
          }

          if (message.contains("숫자")) {
            int cell = Prompt.inputInt("숫자를 입력하세요");
            int maxSize = array.length * array.length;
            while (cell > maxSize) {
              cell = Prompt.inputInt("배열을 초과하였습니다.");
            }
            String value = array[cell / array.length][cell % array.length];
            while (parseIntOrNull(value) == null || cell > maxSize) {
              cell = Prompt.inputInt("해당 자리에 돌이 놓여 있습니다.");
              value = String.valueOf(cell);
            }
            out.writeObject(cell);
            out.flush();
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
