package cs4300;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.util.LinkedList;
import java.util.Random;

// the superclass that the sprites in the game use
public abstract class CgsSprite
{
	// an enum that holds the different weapon types
	// and their characteristics
	public enum WeaponType
	{
		RAPID(20, 80, 1.5f), SPREAD(25, 220, 1.0f), LASER(6, 800, 0f);
		
		public int damage;
		public long coolDown;
		public float speedMod;
		WeaponType(int damage, long coolDown, float speedMod)
		{
			this.damage = damage;
			this.coolDown = coolDown;
			this.speedMod = speedMod;
		}
	}
	
	protected GeneralPath baseOutline;
	protected GeneralPath transOutline;
	protected double xPos, yPos;		// the position of the center of the sprite
	protected double engineX, engineY;	// the position of the engine on the sprite, relative to its center
	protected Color bodyColor, lineColor;
	protected Stroke lineStroke;
	protected AffineTransform aTrans;
	protected double rot;				// the sprite's current CW rotation
	protected float lineWidth;
	protected double xScale, yScale;
	protected Rectangle playArea;		// the area that the sprite must remain within
	protected int maxHealth, health;
	protected int collisionDamage;		// the damage the sprite does when it collides with something
	protected double speedMod;			// scales how fast this sprite is
	protected boolean isFiring;
	private WeaponType weaponType;
	protected long fireCooldown;		// keeps track of how much time must pass before this sprite can fire again
	protected long fireCooldownReset;	// the number of milliseconds in between firings
	protected double xOffset, yOffset;
	protected float fireRateMod;
	
	public GeneralPath getOutline()	{ return (transOutline); }
	public void setFiring(boolean fire) { isFiring = fire; }
	public int getCollisionDamage() { return (collisionDamage); }
	
	protected abstract void updateColor();
	
	// create a new sprite
	// the sprite holds a baseOutline that never changes and is the same for 
	// all sprites of the same type as well an AffineTransformation that defines
	// where the sprite currently is on the screen
	protected CgsSprite(Rectangle playArea, double centerX, double centerY)
	{
		aTrans = new AffineTransform();
		this.playArea = playArea;
		xPos = centerX;
		yPos = centerY;
		rot = 0;
		lineWidth = 5;
		xScale = 1;
		yScale = 1;
		isFiring = false;
		weaponType = WeaponType.RAPID;
		fireCooldown = 0;
		fireCooldownReset = weaponType.coolDown;
		fireRateMod = 0;
	}
	
	// update the sprite based on the amount of time since the last update
	public void update(long delta)
	{
		fireCooldown -= delta*fireRateMod;
	}
	
	// draw the sprite at its current coordinates to the screen
	public void draw(Graphics2D g2d)
	{
		g2d.setStroke(lineStroke);
		g2d.setColor(lineColor);
		
		// set the AffineTransform to match the sprite's current position
		aTrans.setTransform(Math.cos(rot), Math.sin(rot), -Math.sin(rot), Math.cos(rot), 
							xPos, yPos);
		aTrans.scale(xScale, yScale);
		transOutline = (GeneralPath) baseOutline.clone();
		transOutline.transform(aTrans);
		g2d.draw(transOutline);
		
		g2d.setColor(bodyColor);
		g2d.fill(transOutline);
	}
	
	// move the sprite by the passed in offsets
	public void move(double dX, double dY)
	{
		xPos += dX;
		yPos += dY; 
	}
	
	// rotate the sprite so that it is facing the passed in point
	public void rotateToPoint(double x, double y)
	{
		double dx, dy;
		double angle;
		
		dx = x - xPos;
		dy = y - yPos;
		angle = Math.atan2(dy, dx);
		
		// keep the angle between 0 and 2pi
		if (angle < 0)
		{
			angle += 2*Math.PI;
		}
		else if (angle > 2*Math.PI)
		{
			angle -= 2*Math.PI;
		}
		
		rot = angle + Math.PI/2;
	}
	
	// move the sprite to a new position
	// return value is only meaningful for subclasses
	public boolean relocate(double centerX, double centerY)
	{	
		this.xPos = centerX;
		this.yPos = centerY;
		
		return (true);
	}
	
	// have the sprite create a projectile and return it
	// the type and number of projectiles fired is based on the current weapon
	public LinkedList<Projectile> fireProjectile()
	{
		LinkedList<Projectile> projList = new LinkedList<Projectile>();
		
		switch (weaponType)
		{
		case RAPID:
			projList.add(new Projectile(WeaponType.RAPID, playArea, xPos, yPos, rot, this));
			break;
		case SPREAD:
			projList.add(new Projectile(WeaponType.SPREAD, playArea, xPos, yPos, rot-(Math.PI*.08), this));
			projList.add(new Projectile(WeaponType.SPREAD, playArea, xPos, yPos, rot, this));
			projList.add(new Projectile(WeaponType.SPREAD, playArea, xPos, yPos, rot+(Math.PI*.08), this));
			break;
		case LASER:
			projList.add(new Projectile(WeaponType.LASER, playArea, xPos, yPos, rot, this));
			break;
		}
		
		return projList;
	}
	
	// change the current weapon
	public void switchWeapon(WeaponType type)
	{
		weaponType = type;
		fireCooldownReset = type.coolDown;
	}
	
	// update the sprite's health and color after a collision
	public void hitBy(CgsSprite spr)
	{
		health -= spr.getCollisionDamage();
		updateColor();
	}
	
	// set the sprite's health to 0
	public void destroy()
	{
		health = 0;
		updateColor();
	}
	
	// return whether or not the sprite is still alive
	// note that an Enemy is considered not alive while it is
	// in its death-spiral, but remains visible
	public boolean isAlive()
	{
		if (health > 0)
		{
			return (true);
		}
		return (false);
	}
	
	// determine if the sprite should fire
	// based on its current firing status and its weapon cooldown
	public boolean shouldFire()
	{
		if (isFiring == true && fireCooldown < 0)
		{
			fireCooldown = fireCooldownReset;
			return (true);
		}
		return (false);
	}
	
	// check if this sprite has collided with the passed in sprite
	public boolean checkCollision(CgsSprite spr)
	{
		PathIterator pit;
		double coords[] = new double[6];
		GeneralPath sprOl = spr.getOutline();
		int segType;
		
		pit = getOutline().getPathIterator(null);
		pit.currentSegment(coords);
		
		// walk through each point along the outline of this sprite
		while (pit.isDone() == false)
		{
			// currentSegment fills the coords array with points in the current segment
			// if the segment is a line it returns 1
			// if the segment is a bezier curve it returns 3 
			// (which also happens to be the number of reference points used to create the curve)
			// check each point in the segment to see if it is within spr's outline
			segType = pit.currentSegment(coords);
			for (int i = 0; i < segType; i++)
			{
				if (sprOl.contains(coords[0+i*2], coords[1+i*2]) == true)
				{
					// at least one point in this sprite's outline is contained in spr's outline
					return (true);
				}
			}
			pit.next();
		}
		// all points in this sprite's outline are outside of spr's outline
		return (false);
	}
	
	// draw an engine at the sprite's engine position
	protected void drawEngine(Graphics2D g2d)
	{
		double curveX, curveY;
		Random r = CgShooter.rand;
		GeneralPath engine = new GeneralPath();
		
		g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
		
		// the engine is a set of five randomly generated curves
		for (int i = 0; i < 5; i++)
		{
			engine.reset();
			curveX = engineX;
			curveY = engineY;
			engine.moveTo(curveX+=r.nextDouble()*5-2.5, curveY);
			engine.curveTo(curveX+=r.nextDouble()*5-2.5, curveY+=r.nextDouble()+5, 
						   curveX+=r.nextDouble()*5-2.5, curveY+=r.nextDouble()+5, 
						   curveX+=r.nextDouble()*5-2.5, curveY+=r.nextDouble()+5);
			engine.transform(aTrans);
			
			// choose a random reddish-orange color for each curve
			g2d.setColor(new Color(r.nextInt(155)+100, r.nextInt(100)+50, 0));
			g2d.draw(engine);
		}
		
	}
}











