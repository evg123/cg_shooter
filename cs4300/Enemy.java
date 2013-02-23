package cs4300;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;

// the sprite class for the enemies in the game
public class Enemy extends CgsSprite 
{
	// an enum that holds the attributes of the different enemy types
	public enum EnemyType
	{
		TYPE1(50, 50, .2, 125, 0), TYPE2(120, 80, .1, 315, .07f);
		
		public int health, damage, bounty;
		public double speedMod;
		public float fireRateMod;
		
		EnemyType(int health, int damage, double speedMod, int bounty, float fireRateMod)
		{
			this.health = health;
			this.damage = damage;
			this.speedMod = speedMod;
			this.bounty = bounty;
			this.fireRateMod = fireRateMod;
		}
	}
	
	private static final long DEATH_SPIRAL_LEN = 1000;
	private static final GeneralPath type1Outline, type2Outline;
	
	private int bounty; // the number of points awarded for destroying this enemy
	private long deathSpiral;
	private double spiralMod;
	
	public int getBounty() { return (bounty); }
	
	// create an enemy center around the passed in points
	// the enemy will be of the passed in type
	public Enemy(EnemyType type, Rectangle playArea, double centerX, double centerY)
	{
		super(playArea, centerX, centerY);
		
		maxHealth = health = type.health;
		collisionDamage = type.damage;
		speedMod = type.speedMod;
		deathSpiral = 0;
		spiralMod = .015;
		bounty = type.bounty;
		isFiring = true;
		fireRateMod = type.fireRateMod;
		
		// determine the shape of the enemy based on its type
		switch (type)
		{
		case TYPE1:
			baseOutline = type1Outline;
			engineX = 0;
			engineY = 12;
			break;
		case TYPE2:
			baseOutline = type2Outline;
			engineX = 0;
			engineY = 5;
			break;
		default:
			break;
		}
		
		transOutline = (GeneralPath) baseOutline.clone();
		updateColor();
		lineStroke = new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
		xOffset = Math.sin(rot) * speedMod;
		yOffset = -Math.cos(rot) * speedMod;
	}
	
	// enemy shapes are statically allocated to save space
	// each sprite holds a pointer to a baseOutline that never changes
	// and an AffineTransform that defines the sprites position on screen 
	static
	{
		// draw the shape of enemy type 1
		type1Outline = new GeneralPath();
		type1Outline.moveTo( +5, -30);
		type1Outline.lineTo(+10, -10);
		type1Outline.lineTo(+25,  -6);
		type1Outline.lineTo(+25,  +6);
		type1Outline.lineTo(+10,  +6);
		type1Outline.lineTo( +5, +12);
		type1Outline.lineTo( -5, +12);
		type1Outline.lineTo(-10,  +6);
		type1Outline.lineTo(-25,  +6);
		type1Outline.lineTo(-25,  -6);
		type1Outline.lineTo(-10, -10);
		type1Outline.lineTo( -5, -30);
		type1Outline.lineTo( +5, -30);
		
		// draw the shape of enemy type 2
		type2Outline = new GeneralPath();
		type2Outline.moveTo(  0, -25);
		type2Outline.lineTo( +6, -20);
		type2Outline.lineTo( +3, -15);
		type2Outline.lineTo( +3,  -3);
		type2Outline.lineTo(+15,  -3);
		type2Outline.lineTo(+20, -30);
		type2Outline.curveTo(25, -10, 25, 10, 20, 20);
		type2Outline.lineTo(+15, +20);
		type2Outline.lineTo(+15,  +5);
		type2Outline.lineTo(-15,  +5);
		type2Outline.lineTo(-15, +20);
		type2Outline.lineTo(-20, +20);
		type2Outline.curveTo(-25, 10, -25, -10, -20, -30);
		type2Outline.lineTo(-15,  -3);
		type2Outline.lineTo( -3,  -3);
		type2Outline.lineTo( -3, -15);
		type2Outline.lineTo( -6, -20);
		type2Outline.lineTo(  0, -25);
	}
	
	@Override
	// draw the sprite
	public void draw(Graphics2D g2d)
	{
		drawEngine(g2d);
		super.draw(g2d);
	}
	
	@Override
	// update the sprite
	public void update(long delta)
	{
		super.update(delta);
		
		// if the enemy is dead, advance its death-spiral
		if (isAlive() == false)
		{
			// when an enemy dies it begins to spin and shrink
			deathSpiral += delta;
			rot += delta*spiralMod;
			xScale = yScale = 1.0-((double)deathSpiral/DEATH_SPIRAL_LEN);
		}
		
		move(xOffset*delta, yOffset*delta);
	}
	
	// determine if the enemy has completed its death-spiral
	public boolean requiresRemoval()
	{
		if (deathSpiral > DEATH_SPIRAL_LEN)
		{
			return (true);
		}
		return (false);
	}
	
	@Override
	// rotate the sprite to face a point on the screen and update its movement offsets
	public void rotateToPoint(double x, double y)
	{
		super.rotateToPoint(x, y);
		
		xOffset = Math.sin(rot) * speedMod;
		yOffset = -Math.cos(rot) * speedMod;
	}
	
	@Override
	// update the sprite's color based on it's health
	protected void updateColor()
	{
		float colorMod = (float)health/maxHealth;
		if (colorMod < 0)
		{
			colorMod = 0;
		}
		bodyColor = new Color(1-colorMod, colorMod, 0);
		lineColor = Color.BLUE;
	}
}
