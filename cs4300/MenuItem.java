package cs4300;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.RoundRectangle2D;

import cs4300.Menu.MenuAction;

// the superclass of the items that are displayed on a menu
public abstract class MenuItem
{
	protected RoundRectangle2D.Float outline;
	protected MenuAction action;
	private float width, height;
	
	private boolean hovered;
	private boolean selected;
	
	public MenuAction getAction() { return (action); }
	public float getWidth() { return (width); }
	public float getHeight() { return (height); }
	
	public void setHovered(boolean hovered)	{ this.hovered = hovered; }
	public void setSelected(boolean selected) {	this.selected = selected; }
	
	public MenuItem(float width, float height)
	{
		// rect will get it's position when it is added to a menu 
		outline = new RoundRectangle2D.Float(0, 0, width, height, width/4, height/4);
		
		action = MenuAction.INVALID_ACTION; // MenuItem types that return actions will overwrite this
		this.width = width;
		this.height = height;
		hovered = false;
		selected = false;
	}
	
	// draw the item in its current state
	protected void draw(Graphics2D g2d, MenuStyle style)
	{
		if (hovered == true)
		{
			// this item is being hovered over, change its color
			g2d.setColor(style.colors.hoverColor);
		}
		else
		{
			g2d.setColor(style.colors.fillColor);
		}
		g2d.fill(outline);
		if (selected == true)
		{
			// this item is currently being selected in some way
			// change its color
			g2d.setColor(style.colors.selectedColor);
		}
		else
		{
			g2d.setColor(style.colors.trimColor);
		}
		g2d.setStroke(style.stroke);
		g2d.draw(outline);
	}
	
	// determine if this button contains the points passed in
	public boolean contains(float x, float y)
	{
		return (outline.contains(x, y));
	}
	public boolean contains(Point pos)
	{
		return (outline.contains(pos));
	}
}
