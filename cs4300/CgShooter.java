/* *****************************************************************************
 * 	Computer Graphics Shooter
 * 	
 * 	Written by Eric Vande Griek
 * 	
 * 	Version 1.0 - 2/24/2011
 *****************************************************************************/

package cs4300;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.Random;

public class CgShooter extends Applet
{
	private static final long serialVersionUID = 1L;
	private static final int NUM_HIGH_SCORES = 15;
	
	// these define the color scheme most of the game
	private static final Color MAIN_COLOR = Color.WHITE;
	private static final Color BACKGROUND_COLOR = new Color(100, 100, 100);
	private static final Color TRIM_COLOR = Color.GREEN;
	private static final Color HOVER_COLOR = Color.GRAY;
	private static final Color SELECTED_COLOR = Color.ORANGE;
	private static final Color TEXT_COLOR = Color.BLACK;
	
	public static final Random rand = new Random();
	private CgsMenu menu;
	private CgsGameScreen gameScreen;
	private Dimension winSize;
	boolean running;
	LinkedList<HighScore> scoreList;
	ColorScheme colors;
	
	public static void main(String[] args) 
	{
		Frame windowFrame = new Frame("Computer Graphics Shooter");
		windowFrame.addWindowListener(new WindowAdapter() 
								{ public void windowClosing(WindowEvent we) 
								{ System.exit(0); } });
		
		CgShooter game = new CgShooter();
		windowFrame.add(game);
		windowFrame.pack();
		windowFrame.setVisible(true);
		
		game.init();
	}
	
	public CgShooter()
	{		
		winSize = new Dimension(1920, 1200);
		setPreferredSize(winSize);
		
		initHighScores();
		colors = new ColorScheme(MAIN_COLOR, BACKGROUND_COLOR, TRIM_COLOR, HOVER_COLOR, SELECTED_COLOR, TEXT_COLOR);
	}
	
	@Override
	public void init()
	{
		// the program is split into two main parts:
		// 1. the menu system
		// 2. the game screen, where the game is played
		// this loop passes control from one to the other
		while (true)
		{
			showMenu();
			showGameScreen();
		}
	}
	@Override
	public void start()
	{
		
	}
	@Override
	public void stop()
	{
		
	}
	@Override
	public void destroy()
	{
		
	}
	
	// shows the menu system, which includes the main menu and 
	// high score menu
	private void showMenu()
	{
		menu = new CgsMenu(this, winSize, colors, scoreList);
		menu.setPreferredSize(winSize);
		add(menu, 0);
		validate();
		menu.start();
	}
	
	// shows the game screen, where the game is played
	private void showGameScreen()
	{
		HighScore newHs;
		gameScreen = new CgsGameScreen(winSize, colors);
		gameScreen.setPreferredSize(winSize);
		add(gameScreen, 0);
		validate();
		newHs = gameScreen.start();
		
		registerNewScore(newHs);
	}
	
	// this initializes the list of high scores
	// currently high scores only last while the program is open
	// I hope to add high score persistence at a later date
	private void initHighScores()
	{
		HighScore hs;
		
		scoreList = new LinkedList<HighScore>();
		
		for (int i = 0; i < NUM_HIGH_SCORES; i++)
		{
			hs = new HighScore("EMPTY", 0);
			scoreList.add(hs);
		}
	}
	
	// compares a new score to the currently stored high scores
	// if the score is higher than a current high score it will be added to the list
	public void registerNewScore(HighScore newHs)
	{
		int index = 0;
		
		for (HighScore hs : scoreList)
		{
			if (newHs.score > hs.score)
			{
				break;
			}
			index++;
		}
		if (index < CgShooter.NUM_HIGH_SCORES)
		{
			// add this new score and remove the last score
			scoreList.add(index, newHs);
			scoreList.removeLast();
		}
	}
}













