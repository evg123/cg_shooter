package cs4300;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.util.Iterator;
import java.util.LinkedList;
import cs4300.CgsSprite.WeaponType;
import cs4300.Enemy.EnemyType;
import cs4300.Menu.MenuAction;
import cs4300.MenuStyle.LayoutType;

// the class that controls the game screen, where gameplay takes place
public class CgsGameScreen extends Canvas implements MouseListener, KeyListener
{
	public enum GameState
	{
		IN_GAME, PAUSED, GAME_OVER
	}
	
	private static final long serialVersionUID = 1L;
	private static final int DEF_SPAWN_RATE = 500;
	private static final int MAX_NAME_LEN = 32;
	private static final String DEFAULT_NAME = "Your Name Here";
	
	private Dimension winSize;
	private Rectangle playArea;
	private BufferStrategy bStrat;
	private ColorScheme colors;
	
	private boolean running;
	private GameState state;
	private Menu pauseMenu;
	private Menu endMenu;
	private Menu gameMenu;
	private MenuAction pendingAction;
	private Color bgTextureCol1, bgTextureCol2;
	private GradientPaint bgTexture;
	private int bgTextureWidth;
	private float bgScrollPos;
	private float bgScrollMod;
	
	private Player player;
	private boolean keyDownW, keyDownA, keyDownS, keyDownD; 
	private boolean keyDownUp, keyDownLeft, keyDownDown, keyDownRight;
	private boolean keyDownSpace, keyDownEsc;
	private boolean keyDown1, keyDown2, keyDown3;
	private boolean keyDownLmb;
	private LinkedList<Projectile> projectileList;
	private LinkedList<Enemy> enemyList;
	private long spawnCounter;
	private long spawnRate;
	private int score;
	private TextItem scoreDisplay;
	private String name;
	private TextItem nameDisplay;
	private Button rapidBtn, spreadBtn, laserBtn;
	boolean dragging;
	
	public CgsGameScreen(Dimension windowSize, ColorScheme colors)
	{
		winSize = windowSize;
		name = DEFAULT_NAME;
		this.colors = colors;
	}
	
	// starts the game and returns the final score when the game is over
	public HighScore start()
	{
		setIgnoreRepaint(true);
		
		createBufferStrategy(2);
		bStrat = getBufferStrategy();
		
		createPauseMenu();
		createEndMenu();
		createGameMenu();
		initBg();
		initEnemies();
		initPlayer();
		initKeys();
		
		addMouseListener(this);
		addKeyListener(this);
		requestFocus();
		
		pendingAction = MenuAction.INVALID_ACTION;
		running = true;
		state = GameState.IN_GAME;
		score = 0;
		dragging = false;
		
		mainLoop();
		return (new HighScore(name, score));
	}
	
	// sets up the pause menu, which is displayed while the game is paused
	private void createPauseMenu()
	{
		float width, height;
		Button btn;
		MenuStyle style = new MenuStyle(LayoutType.LAYOUT_VERTICAL, colors, 5);
		
		pauseMenu = new Menu("Paused", winSize.width*.5f, winSize.height*.6f, winSize.width*.3f, winSize.height*.4f, style);
		width = pauseMenu.getWidth()*0.7f;
		height = pauseMenu.getHeight()*0.15f;
		
		btn = new Button("Return to Game", MenuAction.PM_RESUME, width, height);
		pauseMenu.add(btn);
		btn = new Button("Main Menu", MenuAction.PM_MAIN_MENU, width, height);
		pauseMenu.add(btn);
		btn = new Button("Exit Game", MenuAction.PM_EXIT_GAME, width, height);
		pauseMenu.add(btn);
	}

	// sets up the end game menu, which is displayed when the game is ended
	// this happens when either the player runs out of health, 
	// or ends the game from the pause menu
	private void createEndMenu()
	{
		float width, height;
		Button btn;
		TextItem item;
		MenuStyle style = new MenuStyle(LayoutType.LAYOUT_VERTICAL, colors, 5);
		
		endMenu = new Menu("Game Over, Man!", winSize.width*.5f, winSize.height*.6f, 
						   winSize.width*.3f, winSize.height*.5f, style);
		width = pauseMenu.getWidth()*0.7f;
		height = pauseMenu.getHeight()*0.15f;
		
		item = new TextItem("Game Over!", 32, false, 0, 0);
		endMenu.add(item);
		item = new TextItem("", 0, false, 0, 0); // this spacer adds some whitespace to the menu
		endMenu.add(item);
		item = new TextItem("Enter Your Name:", 18, false, 0, 0);
		endMenu.add(item);
		nameDisplay = new TextItem(name, 16, false, 0, 0);
		endMenu.add(nameDisplay);
		item = new TextItem("", 0, false, 0, 0);
		endMenu.add(item);
		btn = new Button("Done", MenuAction.EM_DONE, width, height);
		endMenu.add(btn);
	}
	
	// sets up the game menu, which is displayed at the top of the screen while the game is in progress
	private void createGameMenu()
	{
		float itemWidth, itemHeight, menuHeight;
		Button btn;
		TextItem spacer;
		MenuStyle style = new MenuStyle(LayoutType.LAYOUT_HORIZONTAL, colors, 5);
		
		menuHeight = winSize.height*.1f;
		gameMenu = new Menu("", winSize.width/2, menuHeight/2, winSize.width, menuHeight, style);
		itemWidth = gameMenu.getWidth()*0.1f;
		itemHeight = gameMenu.getHeight()*0.7f;
		
		rapidBtn = new Button("Rapid", MenuAction.GM_RAPID, itemWidth, itemHeight);
		gameMenu.add(rapidBtn);
		spreadBtn = new Button("Spread", MenuAction.GM_SPREAD, itemWidth, itemHeight);
		gameMenu.add(spreadBtn);
		laserBtn = new Button("Laser", MenuAction.GM_LASER, itemWidth, itemHeight);
		gameMenu.add(laserBtn);
		spacer = new TextItem("", 0, false, 0, 0);
		gameMenu.add(spacer);
		scoreDisplay = new TextItem("SCORE: "+score, 32, false, 0, 0);
		gameMenu.add(scoreDisplay);
		gameMenu.add(spacer);
		btn = new Button("Pause", MenuAction.GM_PAUSE, itemWidth, itemHeight);
		gameMenu.add(btn);
	}
	
	// sets up the scrolling background
	private void initBg()
	{
		// the textured effect of the background is made by overlaying 
		// a translucent pattern on a solid color (gray by default)
		bgTextureCol1 = new Color(0,0,0,170);
		bgTextureCol2 = new Color(0,0,0,90);
		bgTextureWidth = 90;
		bgScrollPos = bgTextureWidth*2;
		bgScrollMod = 0.2f;
		bgTexture = new GradientPaint(0, 0, bgTextureCol1, bgTextureWidth, 0, bgTextureCol2, true);
		
		// the play area is the screen size minus the game menu at the top of the screen
		playArea = new Rectangle(0, (int)gameMenu.getHeight(), winSize.width, 
								(int)(winSize.height-gameMenu.getHeight()));
	}
	
	// sets up the list of enemies, which is empty at first
	private void initEnemies()
	{
		enemyList = new LinkedList<Enemy>();
		
		spawnCounter = 0;
		spawnRate = DEF_SPAWN_RATE;
	}
	
	// creates the player's sprite and the list that holds projectiles
	private void initPlayer()
	{
		player = new Player(playArea, winSize.width/10, winSize.height/2);
		projectileList = new LinkedList<Projectile>();
		switchWeapon(WeaponType.RAPID);
	}
	
	// initializes all tracked keys to false (not pressed)
	private void initKeys()
	{
		keyDownW = keyDownA = keyDownS = keyDownD = false;
		keyDownUp = keyDownLeft = keyDownDown = keyDownRight = false;
		keyDownSpace = keyDownEsc = false;
		keyDown1 = keyDown2 = keyDown3 = false;
		keyDownLmb = false;
	}
	
	// this loop runs while the program is displaying the game screen
	// it is exited when the player dies or exits the game
	private void mainLoop()
	{
		long lastTick, curTick, delta;
		lastTick = System.currentTimeMillis();
		
		while (running == true)
		{
			// calculate delta - the number of milliseconds 
			// since this loop last ran
			// delta is used to scale many operations and 
			// increment/decrement timers
			curTick = System.currentTimeMillis();
			delta = curTick - lastTick;
			lastTick = curTick;
			
			Graphics2D g2d = (Graphics2D)bStrat.getDrawGraphics();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			updateGame(delta);
			redraw(g2d, delta);
			
			g2d.dispose();
			bStrat.show();
			
			// suspend the thread for a few milliseconds before processing the next frame
			// aim for about 60 frames per second
			try 
			{
				Thread.sleep(16);
			} 
			catch (InterruptedException ex) 
			{
				ex.printStackTrace();
			}
		}
	}
	
	// call the appropriate update methods based on game state
	private void updateGame(long delta)
	{
		handleUserInput(delta);
		
		switch (state)
		{
		case IN_GAME:
			updateProjectiles(delta);
			updateEnemies(delta);
			updatePlayer(delta);
			updateScoreDisplay();
			break;
		case GAME_OVER:
			updateNameDisplay();
			break;
		default:
			break;
		}
		
		if (pendingAction != MenuAction.INVALID_ACTION)
		{
			handleAction();
		}
	}
	
	// redraw the game screen and any active menus
	private void redraw(Graphics2D g2d, long delta)
	{
		drawBackground(g2d, delta);
		
		drawProjectiles(g2d);
		drawEnemies(g2d);
		player.draw(g2d);
		gameMenu.draw(g2d);
		
		if (state == GameState.PAUSED)
		{
			pauseMenu.draw(g2d);
		}
		if (state == GameState.GAME_OVER)
		{
			endMenu.draw(g2d);
		}
	}
	
	// draw the background
	// advance the background movement if the game is running
	private void drawBackground(Graphics2D g2d, long delta)
	{
		g2d.setColor(colors.backgroundColor);
		g2d.fillRect(0, 0, winSize.width, winSize.height);
		
		if (state == GameState.IN_GAME)
		{
			bgScrollPos -= delta*bgScrollMod;
		}
		if (bgScrollPos <= 0)
		{
			bgScrollPos += bgTextureWidth*2;
		}
		
		bgTexture = new GradientPaint(bgScrollPos, 0, bgTextureCol1, bgTextureWidth+bgScrollPos, 0, bgTextureCol2, true);	
		g2d.setPaint(bgTexture);
		g2d.fillRect(0, 0, winSize.width, winSize.height);	
	}
	
	// draw the projectiles in the projectile list
	private void drawProjectiles(Graphics2D g2d)
	{
		for (Projectile proj : projectileList)
		{
			proj.draw(g2d);
		}
	}
	
	// draw the enemies in the enemy list
	private void drawEnemies(Graphics2D g2d)
	{
		for (Enemy e :enemyList)
		{
			e.draw(g2d);
		}
	}
	
	// loop over all of the projectiles in the projectile list
	// update and remove as necessary
	private void updateProjectiles(long delta)
	{
		Projectile proj;
		Iterator<Projectile> it = projectileList.iterator();
		
		while (it.hasNext() == true)
		{
			proj = it.next();
			proj.update(delta);
			
			if (proj.isAlive() == false)
			{
				// remove projectiles with 0 health remaining
				// (expired lasers)
				it.remove();
				continue;
			}
			
			if (playArea.contains(proj.xPos, proj.yPos) == false)
			{
				// projectile is off the screen, remove it from the list
				it.remove();
				continue;
			}
			
			// check if this projectile has struck an enemy or the player
			if (checkCollision(proj) == true)
			{
				if (proj.removeOnCollision() == true)
				{
					// only remove projectiles that are consumed when 
					// they impact a sprite
					it.remove();
				}
			}
		}
	}
	
	// loop over the enemies in play and update or remove as necessary
	// also spawn new enemies
	private void updateEnemies(long delta)
	{
		Enemy e;
		Iterator<Enemy> it = enemyList.iterator();
		
		while (it.hasNext() == true)
		{
			e = it.next();
			e.update(delta);
			
			if (e.xPos < 0)
			{
				// enemy has made it past the left side of the screen, 
				// remove it from the list
				it.remove();
				continue;
			}
			if (e.requiresRemoval() == true)
			{
				// enemy has been destroyed
				// add it's bounty to the player's score
				score += e.getBounty();
				it.remove();
			}
			if (e.shouldFire() == true)
			{
				// the enemy has fired a projectile
				// add it to the projectile list
				projectileList.addAll(e.fireProjectile());
			}
		}
		
		// spawn a new enemy if the spawn timer has expired
		spawnCounter += delta;
		if (spawnCounter >= spawnRate)
		{
			e = new Enemy(nextEnemyType(), playArea, 0, 0);
			e.relocate(playArea.width+e.getOutline().getBounds().width/2, CgShooter.rand.nextInt(playArea.height+1));
			
			// point the enemy at the player's current position
			e.rotateToPoint(player.xPos, player.yPos);
			enemyList.add(e);
			
			// decrement the spawn counter
			spawnCounter -= spawnRate;
		}
	}
	
	// generate a semi-random enemy type
	private EnemyType nextEnemyType()
	{
		int type = CgShooter.rand.nextInt(10);
		if (type < 7) // 0 to 6 returns type1
		{
			return (EnemyType.TYPE1);
		}
		else // 7 to 9 returns type2
		{
			return (EnemyType.TYPE2);
		}
	}
	
	// update the player's sprite
	private void updatePlayer(long delta)
	{
		player.update(delta);
		if (player.shouldFire() == true)
		{
			// the player has fired a projectile
			projectileList.addAll(player.fireProjectile());
		}
		
		// check if the player has collided with an enemy
		for (Enemy e : enemyList)
		{
			if ((e.isAlive() == true) && (player.checkCollision(e)))
			{
				// the player crashed into an enemy
				// damage the player and destroy the enemy
				player.hitBy(e);
				e.destroy();
			}
		}
		
		if (player.isAlive() == false)
		{
			// the player is dead, end the game
			state = GameState.GAME_OVER;
		}
	}
	
	// update the score display with the player's current score
	private void updateScoreDisplay()
	{
		scoreDisplay.setText("SCORE: "+score);
	}
	
	// update the name display to reflect what the player has
	// typed in
	private void updateNameDisplay()
	{
		nameDisplay.setText(name);
	}
	
	// check if the passed in projectile has collided with an enemy or the player
	// returns true if a collision occured
	private boolean checkCollision(Projectile proj)
	{
		Enemy e;
		int hits = 0;
		Iterator<Enemy> it = enemyList.iterator();
		
		// loop over the enemies in the enemy list
		while (it.hasNext() == true)
		{
			e = it.next();
			
			if (proj.isCreator(e))
			{
				// sprites can't shoot themselves
				continue;
			}
			
			if ((e.isAlive() == true) && (proj.checkCollision(e) == true))
			{
				// enemy was hit by projectile
				e.hitBy(proj);
				hits++;
			}
		}
		if (hits > 0)
		{
			// return true if an enemy was hit by this projectile
			return (true);
		}
		
		if (proj.isCreator(player))
		{
			// the player can't shoot it's self
			return (false);
		}
		
		if (proj.checkCollision(player) == true)
		{
			// the player was hit by the projectile
			player.hitBy(proj);
			return (true);
		}
		
		// no one was hit by this projectile
		return (false);
	}
	
	// set the game state to paused
	private void pause()
	{
		state = GameState.PAUSED;
	}
	// unpause the game
	private void unPause()
	{
		state = GameState.IN_GAME;
	}
	
	// process any user input, depending on the current game state
	private void handleUserInput(long delta)
	{
		Point mPos = getMousePosition();
		
		switch (state)
		{
		case PAUSED:
			if (mPos != null)
			{
				// tell the pause menu where the mouse currently is
				pauseMenu.updateMousePos(mPos);
			}
			if (keyDownEsc == true)
			{
				// the escape key unpaues the game
				unPause();
				keyDownEsc = false;
			}
			break;
		case GAME_OVER:
			if (mPos != null)
			{
				// tell the end game menu where the mouse currently is
				endMenu.updateMousePos(mPos);
			}
			break;
		case IN_GAME:
			if (mPos != null)
			{
				// tell the game menu where the mouse currently is
				gameMenu.updateMousePos(mPos);
				
				// if the player is being dragged around we need to update the 
				// player's sprite's coordinates
				if (dragging == true)
				{
					if (player.relocate(mPos.x, mPos.y) == false)
					{
						// the mouse left the play area, stop dragging
						dragging = false;
					}
				}
				else
				{
					// point the player towards the mouse
					player.rotateToPoint(mPos.x, mPos.y);
				}
			}
			
			// handle the state of several keys
			if (keyDownEsc == true)
			{
				// the escape key opens the pause menu
				pause();
				keyDownEsc = false;
			}
			// the player can be moved witht he arrow keys 
			// or w a s d
			if ((keyDownW == true) || (keyDownUp == true))
			{
				player.move(0, -player.speedMod*delta);
			}
			if ((keyDownA == true) || (keyDownLeft == true))
			{
				player.move(-player.speedMod*delta, 0);
			}
			if ((keyDownS == true) || (keyDownDown == true))
			{
				player.move(0, player.speedMod*delta);
			}
			if ((keyDownD == true) || (keyDownRight == true))
			{
				player.move(player.speedMod*delta, 0);
			}
			// the first three number keys can be used to switch weapons
			if (keyDown1 == true)
			{
				switchWeapon(WeaponType.RAPID);
				keyDown1 = false;
			}
			if (keyDown2 == true)
			{
				switchWeapon(WeaponType.SPREAD);
				keyDown2 = false;
			}
			if (keyDown3 == true)
			{
				switchWeapon(WeaponType.LASER);
				keyDown3 = false;
			}
			// space and the left mouse button fire the player's weapons
			if ((keyDownSpace == true) || 
				((keyDownLmb == true) && (dragging == false)))
			{
				player.setFiring(true);
			}
			else
			{
				player.setFiring(false);
			}
			break;
		default:
			break;
		}
	}
	
	// perform the necessary action based on the current pendingAction
	// pendingAction is set when a button is pressed on a menu
	private void handleAction()
	{
		switch (state)
		{
		case PAUSED:
			switch (pendingAction)
			{
			case PM_RESUME:
				unPause();
				break;
			case PM_MAIN_MENU:
				state = GameState.GAME_OVER;
				break;
			case PM_EXIT_GAME:
				exitGame();
				break;
			default:
				break;
			}
			break;
		case GAME_OVER:
			switch (pendingAction)
			{
			case EM_DONE:
				// the player is done entering his/her name
				// return them to the main menu
				showMainMenu();
				break;
			default:
				break;
			}
			break;
		case IN_GAME:
			switch (pendingAction)
			{
			case GM_RAPID:
				switchWeapon(CgsSprite.WeaponType.RAPID);
				break;
			case GM_SPREAD:
				switchWeapon(CgsSprite.WeaponType.SPREAD);
				break;
			case GM_LASER:
				switchWeapon(CgsSprite.WeaponType.LASER);
				break;
			case GM_PAUSE:
				pause();
				break;
			}
			break;
		default:
			break;
		}

		pendingAction = MenuAction.INVALID_ACTION;
	}
	
	// switch the player's current weapon and update 
	// the currently highlighted button on the game menu
	private void switchWeapon(CgsSprite.WeaponType type)
	{
		player.switchWeapon(type);
		switch (type)
		{
		case RAPID:
			unSelectWeapons();
			rapidBtn.setSelected(true);
			break;
		case SPREAD:
			unSelectWeapons();
			spreadBtn.setSelected(true);
			break;
		case LASER:
			unSelectWeapons();
			laserBtn.setSelected(true);
			break;
		default:
			break;
		}
	}
	// removes the selected status from all weapon selection buttons
	private void unSelectWeapons()
	{
		rapidBtn.setSelected(false);
		spreadBtn.setSelected(false);
		laserBtn.setSelected(false);
	}
	
	// exit the game screen and return to the main menu
	private void showMainMenu()
	{
		running = false;
	}
	
	@Override
	// a mouse button has been depressed
	public void mousePressed(MouseEvent event)
	{
		Point mPos = event.getPoint();
		
		if (mPos == null)
		{
			return;
		}
		
		if (state == GameState.IN_GAME)
		{
			if (event.getButton() == MouseEvent.BUTTON1)
			{
				if (player.getOutline().contains(mPos) == true)
				{
					dragging = true;
				}
				keyDownLmb = true;
			}
		}
	}
	@Override
	// a mouse button has been clicked
	public void mouseClicked(MouseEvent event)
	{
		Point mPos = event.getPoint();
		
		if (mPos == null)
		{
			return;
		}
		
		// check if the click occurred on a button
		switch (state)
		{
		case PAUSED:
			if (pauseMenu.contains(mPos) == true)
			{
				pendingAction = pauseMenu.handleClick(mPos);
			}
			break;
		case GAME_OVER:
			if (endMenu.contains(mPos) == true)
			{
				pendingAction = endMenu.handleClick(mPos);
			}
			break;
		case IN_GAME:
			if (gameMenu.contains(mPos) == true)
			{
				pendingAction = gameMenu.handleClick(mPos);
			}
			break;
		default:
			break;
		}
	}
	
	@Override
	// not used
	public void mouseEntered(MouseEvent event) {}
	
	@Override
	// the mouse left the screen
	public void mouseExited(MouseEvent event)
	{
		// clear the hover status from any buttons
		pauseMenu.clearHovered();
		endMenu.clearHovered();
		gameMenu.clearHovered();
		
		// the mouse left the screen, stop dragging the player
		dragging = false;
	}
	@Override
	// a mouse button was released
	public void mouseReleased(MouseEvent event)
	{
		if (event.getButton() == MouseEvent.BUTTON1)
		{
			keyDownLmb = false;
			dragging = false;
		}
	}
	@Override
	// keep track of the state of certain keys
	public void keyPressed(KeyEvent event)
	{
		switch(event.getKeyCode())
		{
		case KeyEvent.VK_W:
			keyDownW = true;
			break;
		case KeyEvent.VK_A:
			keyDownA = true;
			break;
		case KeyEvent.VK_S:
			keyDownS = true;
			break;
		case KeyEvent.VK_D:
			keyDownD = true;
			break;
		case KeyEvent.VK_UP:
			keyDownUp = true;
			break;
		case KeyEvent.VK_LEFT:
			keyDownLeft = true;
			break;
		case KeyEvent.VK_DOWN:
			keyDownDown = true;
			break;
		case KeyEvent.VK_RIGHT:
			keyDownRight = true;
			break;
		case KeyEvent.VK_SPACE:
			keyDownSpace = true;
			break;
		}
	}
	@Override
	// keep track of the state of certain keys
	public void keyReleased(KeyEvent event)
	{
		switch(event.getKeyCode())
		{
		case KeyEvent.VK_W:
			keyDownW = false;
			break;
		case KeyEvent.VK_A:
			keyDownA = false;
			break;
		case KeyEvent.VK_S:
			keyDownS = false;
			break;
		case KeyEvent.VK_D:
			keyDownD = false;
			break;
		case KeyEvent.VK_UP:
			keyDownUp = false;
			break;
		case KeyEvent.VK_LEFT:
			keyDownLeft = false;
			break;
		case KeyEvent.VK_DOWN:
			keyDownDown = false;
			break;
		case KeyEvent.VK_RIGHT:
			keyDownRight = false;
			break;
		case KeyEvent.VK_SPACE:
			keyDownSpace = false;
			break;
		// for the rest of the keys, we don't care if they are held down
		case KeyEvent.VK_ESCAPE:
			keyDownEsc = !keyDownEsc;
			break;
		case KeyEvent.VK_1:
			keyDown1 = !keyDown1;
			break;
		case KeyEvent.VK_2:
			keyDown2 = !keyDown2;
			break;
		case KeyEvent.VK_3:
			keyDown3 = !keyDown3;
			break;
		}
	}
	@Override
	// a key was typed
	public void keyTyped(KeyEvent event)
	{
		char key;
		int strLen;
		
		if (state == GameState.GAME_OVER)
		{
			// the player is typing his/her name
			key = event.getKeyChar();
			strLen = name.length();
			if ((key == 8) && (strLen > 0)) // key pressed was backspace
			{
				// delete the last character
				name = name.substring(0, strLen-1);
			}
			else if ((strLen < MAX_NAME_LEN) && 
					(Character.isIdentifierIgnorable(key) == false))
			{
				// append the character to the end of the name
				name += key;
			}
		}
	}
	
	// quit the program entirely
	private void exitGame()
	{
		System.exit(0);
	}
}









