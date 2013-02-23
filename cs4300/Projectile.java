package cs4300;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

// the sprite class for the projectiles fired in the game
public class Projectile extends CgsSprite 
{
	private static final int MAX_LASER_GROWTH = 12;
	private static final float LASER_GROWTH_MOD = 0.02f;
	
	private CgsSprite creator;
	private static final GeneralPath rapidOutline, spreadOutline, laserOutline;
	WeaponType type;
	
	// create a projectile of the passed in type
	public Projectile(WeaponType type, Rectangle playArea, double centerX, double centerY, double rot, CgsSprite creator)
	{
		super(playArea, centerX, centerY);
		
		maxHealth = health = 1;
		collisionDamage = type.damage;
		speedMod = type.speedMod;
		this.creator = creator;
		this.rot = rot;
		this.type = type;
		fireRateMod = 0;
		
		// each projectile type has a different look
		switch (type)
		{
		case RAPID:
			baseOutline = rapidOutline;
			break;
		case SPREAD:
			baseOutline = spreadOutline;
			break;
		case LASER:
			baseOutline = laserOutline;
			lineWidth = .01f;
			break;
		default:
			// this is an error
			baseOutline = new GeneralPath();
			break;
		}
		transOutline = (GeneralPath) baseOutline.clone();
		
		// default colors
		bodyColor = Color.RED;
		lineColor = Color.BLUE;
		
		lineStroke = new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
		xOffset = Math.sin(rot) * speedMod;
		yOffset = -Math.cos(rot) * speedMod;
	}
	
	// Statically create the baseOutlines for the different types of projectiles  
	static
	{
		// draw the shape of a rapid projectile
		rapidOutline = new GeneralPath();
		rapidOutline.moveTo(+5, -5);
		rapidOutline.lineTo(+5, +5);
		rapidOutline.moveTo(-5, +5);
		rapidOutline.lineTo(-5, -5);
		
		// draw the shape of a spread projectile
		spreadOutline = new GeneralPath();
		spreadOutline.moveTo(+2, -2);
		spreadOutline.lineTo(+2, +2);
		spreadOutline.lineTo(-2, +2);
		spreadOutline.lineTo(-2, -2);
		spreadOutline.lineTo(+2, -2);
		
		// draw the shape of a laser projectile
		laserOutline = new GeneralPath();
		laserOutline.moveTo(0, 0);
		laserOutline.lineTo(0, -4096); // yes this is terrible
	}
	
	// return true if the passed in sprite created this projectile
	public boolean isCreator(CgsSprite sprite)
	{
		if (sprite == creator)
		{
			return (true);
		}
		return (false);
	}
	
	@Override
	// update the sprite
	public void update(long delta)
	{
		super.update(delta);
		switch (type)
		{
		case RAPID:
			move(xOffset*delta, yOffset*delta);
			break;
		case SPREAD:
			move(xOffset*delta, yOffset*delta);
			break;
		case LASER:
			updateLaser(delta);
			break;
		default:
			break;
		}
	}
	
	// update a laser projectile
	private void updateLaser(long delta)
	{
		// lasers don't travel across the screen
		// instead they appear as a narrow beam and slowly widen before disappearing
		if (lineWidth < MAX_LASER_GROWTH)
		{
			lineWidth += delta*LASER_GROWTH_MOD;
			lineStroke = new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
		}
		else
		{
			// the laser has reached its maximum width, remove it
			destroy();
		}
	}
	
	@Override
	// not used for projectiles
	protected void updateColor() {}
	
	@Override
	// check if this projectile has collided with the passed in sprite
	public boolean checkCollision(CgsSprite spr)
	{
		PathIterator pit;
		double coords[] = new double[6];
		double prevX, prevY;
		GeneralPath ol = getOutline();
		Point2D endPoint = ol.getCurrentPoint();
		int segType;
		
		// laser collision is more complicated
		if (type == WeaponType.LASER)
		{
			pit = spr.getOutline().getPathIterator(null);
			pit.currentSegment(coords);
			prevX = coords[0];
			prevY = coords[1];
			
			// walk through each point along the outline of the sprite
			// checking if the laser intersects any line in the sprite
			while (pit.isDone() == false)
			{
				// currentSegment fills the coords array with points in the current segment
				// if the segment is a line it returns 1
				// if the segment is a bezier curve it returns 3 
				// (which also happens to be the number of reference points used to create the curve)
				// treat a bezier curve as a series of lines between it's reference points
				segType = pit.currentSegment(coords);
				for (int i = 0; i < segType; i++)
				{
					if (Line2D.linesIntersect(xPos, yPos, endPoint.getX(), endPoint.getY(), 
											  prevX, prevY, coords[0+i*2], coords[1+i*2]) == true)
					{
						// an intersection was detected, meaning this sprite was hit
						return (true);
					}
					prevX = coords[0+i*2];
					prevY = coords[1+i*2];
				}
				pit.next();
			}
			// no intersection was found after checking each line segment in the outline
			return (false);
		}
		// other projectile types use standard collision detection
		return (super.checkCollision(spr));
	}
	
	// determine if the sprite should be removed after it impacts another sprite
	public boolean removeOnCollision()
	{
		if (type == WeaponType.LASER)
		{
			// lasers can hit multiple sprites (and the same sprite multiple times)
			return (false);
		}
		return (true);
	}
}






