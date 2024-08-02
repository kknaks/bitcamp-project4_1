package bitcamp.myapp.command;

import bitcamp.command.Command;
import bitcamp.context.ApplicationContext;
import bitcamp.util.Prompt;
import bitcamp.vo.Room;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

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
    System.out.println("포트 번호: " + room.getPort());

    try {
      Thread.sleep(2000); // 2초 대기

      try (Socket socket = new Socket("localhost", room.getPort());
          ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
          ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
        System.out.println("서버연결완료");
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
            String value = array[cell / array.length][cell % array.length];
            int maxSize = array.length * array.length;
            while (parseIntOrNull(value) == null || cell > maxSize) {
              cell = Prompt.inputInt("숫자를 다시 입력하세요");
              value = String.valueOf(cell);
            }
            System.out.println(value);
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
