package cs4300;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;

// the sprite class used for the player's ship
public class Player extends CgsSprite
{
	public Player(Rectangle playArea, double centerX, double centerY)
	{
		super(playArea, centerX, centerY);
		
		maxHealth = health = 300;
		collisionDamage = 0;
		speedMod = 0.2f;
		fireRateMod = 1.0f;

		// draw the shape of the player
		baseOutline = new GeneralPath();
		baseOutline.moveTo(+25,  -6);
		baseOutline.lineTo(+25,  +6);
		baseOutline.lineTo(+10,  +6);
		baseOutline.curveTo(+3, +20, -3, +20, -10,  +6);
		baseOutline.lineTo(-25,  +6);
		baseOutline.lineTo(-25,  -6);
		baseOutline.lineTo(-10, -10);
		baseOutline.curveTo(-5, -40, +5, -40, +10, -10);
		baseOutline.lineTo(+25,  -6);
		engineX = 0;
		engineY = 18;
		
		transOutline = (GeneralPath) baseOutline.clone();
		updateColor();
		lineStroke = new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
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
	}
	
	@Override
	// move the sprite by the passed in offsets
	// make sure the sprite does not leave the play area
	public void move(double dX, double dY)
	{
		Rectangle bounds;
		super.move(dX, dY);
		
		bounds = transOutline.getBounds();
		if (xPos+bounds.width/2 > playArea.getMaxX())
		{
			xPos = playArea.getMaxX()-bounds.getWidth()/2;
		}
		else if (xPos-bounds.width/2 < playArea.getMinX())
		{
			xPos = playArea.getMinX()+bounds.getWidth()/2;
		}
		
		if (yPos+bounds.height/2 > playArea.getMaxY())
		{
			yPos = playArea.getMaxY()-bounds.getHeight()/2;
		}
		else if (yPos-bounds.height/2 < playArea.getMinY())
		{
			yPos = playArea.getMinY()+bounds.getHeight()/2;
		}
	}
	
	@Override
	// move the sprite to the passed in location
	// make sure it stays within the play area
	// returns false if the point was outside the play Area
	public boolean relocate(double centerX, double centerY)
	{
		Rectangle bounds;
		
		super.relocate(centerX, centerY);
		bounds = transOutline.getBounds();
		
		if (centerX+bounds.width/2 > playArea.getMaxX())
		{
			centerX = playArea.getMaxX()-bounds.getWidth()/2;
			return (false);
		}
		else if (centerX-bounds.width/2 < playArea.getMinX())
		{
			centerX = playArea.getMinX()+bounds.getWidth()/2;
			return (false);
		}
		
		if (centerY+bounds.height/2 > playArea.getMaxY())
		{
			centerY = playArea.getMaxY()-bounds.getHeight()/2;
			return (false);
		}
		else if (centerY-bounds.height/2 < playArea.getMinY())
		{
			centerY = playArea.getMinY()+bounds.getHeight()/2;
			return (false);
		}
		return (true);
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
		// the sprite starts green and becomes red as it takes damage
		bodyColor = new Color(1-colorMod, colorMod, 0f);
		lineColor = Color.WHITE;
	}
}






