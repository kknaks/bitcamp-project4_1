# Toy_Project

- Java 기초프로그래밍 중 4번째 프로젝트
- 오목프로그램 만들기

## 프로젝트 소개

- 서버와 클라이언트 상호 통신을 기반한 온라인 오목프로그램
- 쓰레드를 적용하여 독립적인 게임생성 및 상호게임 진행

## 개발환경 및 적용기술

- Language & IDE</li>

  ![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white) ![IntelliJ IDEA](https://img.shields.io/badge/IntelliJIDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white)
- 적용기술 및 패턴
    1) **Network** : Sever Class와 Client Class를 각각 생성하여 Socket을 data 통한 통신
    2) **Thread** : 게임 생성 및 게임 진행을 Thread을 통한 실행으로 독립적인 게임 기능 구현
    3) **Observer 패턴** : 앱 실행 및 종료 시 객체에게 알림 기능 추가

## 목차

- [아키텍처](#아키텍처)
- [기능](#기능)
    - [서버](#서버)
    - [클라이언트](#클라이언트)
- [결과](#결과)
- [보완사항](#보완사항)

## 아키텍처

### 실행 흐름도

![flow](https://github.com/user-attachments/assets/8df66959-f984-458d-b6cf-e2b56b4b5905)

### 서버 UML

![serverUml](https://github.com/user-attachments/assets/e4db89f5-6f28-42a8-9842-a71b6347ee07)

### 클라이언트 UML

![ClientUml](https://github.com/user-attachments/assets/eb51d57a-c1aa-4a03-ba36-26104c6266f5)

## 기능

### 서버

- **서버 초기화**: 서버 애플리케이션을 초기화하고 클라이언트 연결을 대기합니다.
    - `ServerApp.execute()`
    - `RoomServerRunnable` 스레드 시작
      <details>
      <summary> 코드 접기/펼치기</summary>

      ```java
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
      ```
      </details>

- **방 생성 및 관리**: 클라이언트의 요청을 받아 새로운 방을 생성하고 관리
    - `RoomServerRunnable.run()`
    - `ServerApp.addRoom()`
    - `GameServerRunnable` 스레드 시작

- **게임 진행**: 각 방에서 게임을 진행하고 클라이언트 간의 통신을 관리
    - `GameServerRunnable.run()`
    - `ClientHandler.run()`
    - `GameServerRunnable.handlePlayerInput()`

### 클라이언트

- **클라이언트 초기화**: 클라이언트 애플리케이션을 초기화하고 서버에 연결
    - `ClientApp.main()`

- **방 생성**: 새로운 방을 생성하고 서버에 요청
    - `MakeRoomCommand.execute()`

- **방 목록 요청 및 참여**: 서버로부터 방 목록을 요청하고, 특정 방에 참여
    - `JoinGameCommand.execute()`

- **게임 진행**: 서버와의 통신을 통해 게임을 진행
    - `MultiGameCommand.execute()`

## 결과

![화면 기록 2024-08-03 오전 12 48 32](https://github.com/user-attachments/assets/a605a9ea-237a-4389-aa3b-56e7959ee7cb)
