package bitcamp.myapp.listener;

import bitcamp.context.ApplicationContext;
import bitcamp.listener.ApplicationListener;
import bitcamp.menu.MenuGroup;
import bitcamp.menu.MenuItem;

import bitcamp.myapp.command.JoinGameCommand;
import bitcamp.myapp.command.MultiGameCommand;
import bitcamp.myapp.command.MakeRoomCommand;

public class InitApplicationListener implements ApplicationListener {

  @Override
  public void onStart(ApplicationContext ctx) throws Exception {
    MenuGroup mainMenu = new MenuGroup("메인메뉴");

    MenuGroup multiGameMenu = new MenuGroup("멀티게임");
    multiGameMenu.add(new MenuItem("방만들기", new MakeRoomCommand(ctx)));
    multiGameMenu.add(new MenuItem("참여하기", new JoinGameCommand(ctx)));

    mainMenu.add(multiGameMenu);

    mainMenu.setExitMenuTitle("종료");
    ctx.setAttribute("mainMenu", mainMenu);
  }

  @Override
  public void onShutdown(ApplicationContext ctx) throws Exception {
    System.out.println("클라이언트 애플리케이션을 종료합니다.");
    // 종료 작업 수행
  }
}
