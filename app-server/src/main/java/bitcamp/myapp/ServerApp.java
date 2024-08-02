package bitcamp.myapp;

import bitcamp.context.ApplicationContext;
import bitcamp.listener.ApplicationListener;
import bitcamp.listener.InitApplicationListener;
import bitcamp.vo.Game;
import bitcamp.vo.Room;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerApp {
  List<ApplicationListener> listeners = new ArrayList<>();
  ApplicationContext appCtx = new ApplicationContext();
  List<Room> rooms = new ArrayList<>();

  public static void main(String[] args) {
    ServerApp app = new ServerApp();
    app.addApplicationListener(new InitApplicationListener());
    app.execute();
  }

  private void addApplicationListener(ApplicationListener listener) {
    listeners.add(listener);
  }

  private void removeApplicationListener(ApplicationListener listener) {
    listeners.remove(listener);
  }

  void execute() {
    new Thread(new RoomServerRunnable()).start();
  }

  class RoomServerRunnable implements Runnable {
    @Override
    public void run() {
      try (ServerSocket serverSocket = new ServerSocket(8888)) {
        System.out.println("룸 서버가 8888 포트에서 시작되었습니다.");
        while (true) {
          try (Socket socket = serverSocket.accept();
              ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
              ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            // 8888 포트에서 수행할 작업
            System.out.println("포트 8888에서 클라이언트 연결 수락됨");

            String command = (String) in.readObject();

            if (command.equals("GET_ROOMS")) {
              out.writeObject(rooms);
            } else if (command.equals("CREATE_ROOM")) {
              Room room = (Room) in.readObject();
              System.out.println(room.getNo() + "번 방이 생성 되었습니다.");
              rooms.add(room);

              // 방(Room)마다 독립적인 스레드를 실행
              new Thread(new GameServerRunnable(room)).start();
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }


  class GameServerRunnable implements Runnable {
    private Room room;
    private Game game;
    private List<ClientHandler> clients = new ArrayList<>();
    private int currentPlayer = 0;

    public GameServerRunnable(Room room) {
      this.room = room;
      this.game = new Game(room.getSize(), room.getCount());
    }

    @Override
    public void run() {
      try (ServerSocket serverSocket = new ServerSocket(room.getPort())) {
        System.out.println("게임 서버가 " + room.getPort() + " 포트에서 시작되었습니다.");
        for (ApplicationListener listener : listeners) {
          listener.onStart(appCtx);
        }

        while (clients.size() < 2) {
          Socket clientSocket = serverSocket.accept();
          ClientHandler clientHandler = new ClientHandler(clientSocket, clients.size() + 1);
          clients.add(clientHandler);
          new Thread(clientHandler).start();
        }
        if (clients.size() == 2) {
          informStart();
        }
        rooms.remove(room);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    synchronized void informStart() {
      for (ClientHandler client : clients) {
        client.sendMessage("게임 시작");
      }
      broadcastMessage("배열");
      sendNextTurnMessage();
    }

    synchronized void sendNextTurnMessage() {
      clients.get(currentPlayer).sendMessage("Player" + (currentPlayer + 1) + "의 턴입니다. 숫자는?: ");
    }

    synchronized void handlePlayerInput(int playerNumber, int num) {
      if (playerNumber != currentPlayer + 1) {
        clients.get(playerNumber - 1).sendMessage("상대의 턴 입니다.");
        return;
      }

      broadcastMessage("배열");

      currentPlayer = 1 - currentPlayer;

      if (game.isGameOver()) {
        clients.get(currentPlayer).sendMessage("Player" + (currentPlayer + 1) + " 님이 졌습니다!");
        clients.get(1 - currentPlayer).sendMessage("Player" + (currentPlayer) + " 님이 이겼습니다!");
        broadcastMessage("게임 종료");

      } else {
        sendNextTurnMessage();
      }
    }

    private void broadcastMessage(Object message) {
      for (ClientHandler client : clients) {
        client.sendMessage(message);
      }
    }

    class ClientHandler implements Runnable {
      private Socket socket;
      private ObjectInputStream in;
      private ObjectOutputStream out;
      private int player;

      public ClientHandler(Socket clientSocket, int player) {
        this.socket = clientSocket;
        this.player = player;
        try {
          out = new ObjectOutputStream(socket.getOutputStream());
          in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      @Override
      public void run() {
        try {
          while (true) {
            int cell = (int) in.readObject();
            game.put(cell, player);
            handlePlayerInput(player, cell);
          }
        } catch (Exception e) {
          //          e.printStackTrace();
        } finally {
          try {
            in.close();
            out.close();
            socket.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }

      public void sendMessage(Object message) {
        if (!message.equals("배열")) {
          try {
            out.writeObject(message);
            out.flush();
          } catch (IOException e) {
            e.printStackTrace();
          }
        } else {
          try {
            out.writeObject(message);
            out.writeObject(game.getArr());
            out.flush();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }
}
