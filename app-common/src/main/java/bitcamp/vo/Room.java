package bitcamp.vo;

import java.io.Serializable;

public class Room implements Serializable {

  private static final long serialVersionUID = 1L;
  private static final String IP = "127.0.0.1";

  private static int seqRoomNo;
  private static int seqPort = 9000;

  private int no;
  private int port;
  private String title;
  private int size;
  private int count;
  private int memberCount = 1;

  public Room() {

  }

  public static int setSeqRoomNo() {
    return Room.seqRoomNo++;
  }

  public static int setSeqPort() {
    return Room.seqPort++;
  }

  public int getNo() {
    return no;
  }

  public void setNo(int no) {
    this.no = no;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getIp() {
    return IP;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public int getMemberCount() {
    return memberCount;
  }
}

