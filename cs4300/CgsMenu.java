package cs4300;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.util.LinkedList;

import cs4300.Menu.MenuAction;
import cs4300.MenuStyle.LayoutType;

public class CgsMenu extends Canvas implements MouseListener
{
	private static final long serialVersionUID = 1L;
	private static final String GAME_TITLE = "Computer Graphics Shooter";
	private static final String CREDITS = "Eric Vande Griek - 2011";
	
	private enum MenuType
	{
		NONE, MAIN_MENU, HIGH_SCORES_MENU
	}
	
	private CgShooter creator;
	private Menu menu;
	private MenuStyle style;
	private Dimension winSize;
	private BufferStrategy bStrat;
	private LinkedList<HighScore> scoreList;
	
	private boolean switchMenu;
	private MenuType currentMenu;
	private MenuAction pendingAction;
	
	public CgsMenu(CgShooter creator, Dimension windowSize, ColorScheme colors, LinkedList<HighScore> scoreList)
	{
		this.creator = creator;
		winSize = windowSize;
		this.scoreList = scoreList;
		style = new MenuStyle(LayoutType.LAYOUT_VERTICAL, colors, 5);
		pendingAction = MenuAction.INVALID_ACTION;
	}
	
	// start the menu and enter the loop that switches between sub-menus
	public void start()
	{
		setIgnoreRepaint(true);
		
		createBufferStrategy(2);
		bStrat = getBufferStrategy();
		
		addMouseListener(this);	
		requestFocus();
		
		// start with the main menu
		currentMenu = MenuType.MAIN_MENU;
		switchMenu = false;
		
		while (currentMenu != MenuType.NONE)
		{
			switch (currentMenu)
			{
			case MAIN_MENU:
				mainMenu();
				break;
			case HIGH_SCORES_MENU:
				highScoresMenu();
				break;
			default:
				break;
			}
		}
	}
	
	// set up the main menu and enter the main loop
	private void mainMenu()
	{
		float btnWidth, btnHeight;
		Button btn;
		
		menu = new Menu("", winSize.width*.5f, winSize.height*.6f, winSize.width*.3f, winSize.height*.6f, style);
		
		btnWidth = menu.getWidth()*.7f;
		btnHeight = menu.getHeight()*.15f;
		
		btn = new Button("Start Game", MenuAction.MM_START_GAME, btnWidth, btnHeight);
		menu.add(btn);
		btn = new Button("High Scores", MenuAction.MM_HIGH_SCORES, btnWidth, btnHeight);
		menu.add(btn);
		btn = new Button("Exit Game", MenuAction.MM_EXIT_GAME, btnWidth, btnHeight);
		menu.add(btn);
		
		mainLoop();
	}
	
	// set up the high score menu and enter the main loop
	private void highScoresMenu()
	{
		float itemWidth, itemHeight;
		TextItem item;
		int index = 1;
		
		menu = new Menu("High Scores", winSize.width*.5f, winSize.height*.5f, winSize.width*.8f, winSize.height*.95f, style);
		
		itemWidth = menu.getWidth()*.8f;
		itemHeight = menu.getHeight()*.04f;
		
		// create a text item for each entry in the scoreList
		for (HighScore hs : scoreList)
		{
			item = new TextItem(hs.name+": "+hs.score, 12, true, itemWidth, itemHeight);
			menu.add(item);
			index++;
		}
		
		Button btn = new Button("Back", MenuAction.HS_BACK, itemWidth/4, itemHeight);
		menu.add(btn);
		
		mainLoop();
	}
	
	// the main loop for a menu
	// the loop is exited when the current menu sets switchMenu to true
	private void mainLoop()
	{
		Point mPos;
		
		while (switchMenu == false)
		{
			mPos = getMousePosition();
			
			// keep track of where the mouse is on the menu for highlighting purposes
			if (mPos != null)
			{
				menu.updateMousePos(mPos);
			}
			
			// check if the current menu has requested that an action be taken
			if (pendingAction != MenuAction.INVALID_ACTION)
			{
				handleAction();
			}
			
			Graphics2D g2d = (Graphics2D)bStrat.getDrawGraphics();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			redraw(g2d);
			g2d.dispose();
			bStrat.show();
			
			// suspend the thread for a few milliseconds before processing the next frame
			// aim for about 30 frames per second
			try 
			{
				Thread.sleep(33);
			} 
			catch (InterruptedException ex) 
			{
				ex.printStackTrace();
			}
		}
		switchMenu = false;
	}
	
	// redraw the current menu
	private void redraw(Graphics2D g2d)
	{
		g2d.setColor(style.colors.backgroundColor);
		g2d.fillRect(0, 0, winSize.width, winSize.height);
		
		// there are a few special things to draw on the main menu
		if (currentMenu == MenuType.MAIN_MENU)
		{
			// draw the title of the game
			Menu.drawText(g2d, GAME_TITLE, 62, style.colors.fillColor, winSize.width/2-4, winSize.height/8-3);
			Menu.drawText(g2d, GAME_TITLE, 62, style.colors.trimColor, winSize.width/2, winSize.height/8);
			
			// draw the author's name and year
			Menu.drawText(g2d, CREDITS, 16, style.colors.textColor, (winSize.width/7)*6, (winSize.height/12)*11);
		}
		
		menu.draw(g2d);
		creator.repaint();
	}
	
	// process an action that was requested by a menu
	private void handleAction()
	{
		switch (currentMenu)
		{
		case MAIN_MENU:
			switch (pendingAction)
			{
			case MM_START_GAME:
				showGameScreen();
				break;
			case MM_HIGH_SCORES:
				switchToHighScores();
				break;
			case MM_EXIT_GAME:
				exitGame();
				break;
			default:
				break;
			}
		case HIGH_SCORES_MENU:
			switch (pendingAction)
			{
			case HS_BACK:
				switchToMainMenu();
				break;
			default:
				break;
			}
		default:
			break;
		}
		pendingAction = MenuAction.INVALID_ACTION;
	}
	
	// exit the menu system and return to the main class
	// which will show the game screen
	private void showGameScreen()
	{
		currentMenu = MenuType.NONE;
		switchMenu = true;
	}
	
	// show the main menu
	private void switchToMainMenu()
	{
		currentMenu = MenuType.MAIN_MENU;
		switchMenu = true;
	}
	
	// show the high score menu
	private void switchToHighScores()
	{
		currentMenu = MenuType.HIGH_SCORES_MENU;
		switchMenu = true;
	}
	
	// exit the game entirely
	private void exitGame()
	{
		System.exit(0);
	}
	
	@Override
	// not used
	public void mousePressed(MouseEvent event) {}
	
	@Override
	// the mouse was clicked, pass it on to the current menu
	public void mouseClicked(MouseEvent event)
	{
		Point mPos = event.getPoint();
		
		if (mPos != null)
		{
			if (menu.contains(mPos))
			{
				pendingAction = menu.handleClick(mPos);
			}
		}
	}
	
	@Override
	// not used
	public void mouseEntered(MouseEvent event) {}
	
	@Override
	// the mouse left the screen, stop highlighting any buttons
	public void mouseExited(MouseEvent event)
	{
		menu.clearHovered();
	}
	
	@Override
	// not used
	public void mouseReleased(MouseEvent event)	{}
}
