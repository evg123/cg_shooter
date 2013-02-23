package cs4300;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedList;

import cs4300.MenuStyle.LayoutType;

public class Menu
{
	public enum MenuAction
	{
		INVALID_ACTION, 
		MM_START_GAME, MM_HIGH_SCORES, MM_OPTIONS, MM_EXIT_GAME,
		HS_BACK,
		PM_RESUME, PM_MAIN_MENU, PM_EXIT_GAME,
		EM_DONE,
		GM_RAPID, GM_SPREAD, GM_LASER, GM_PAUSE
	}
	
	private RoundRectangle2D.Float outline;
	private MenuStyle style;
	private LinkedList<MenuItem> items;
	
	public float getXPos() { return (outline.x); }
	public float getYPos() { return (outline.y); }
	public float getWidth() { return (outline.width); }
	public float getHeight() { return (outline.height); }
	
	// creates a menu centered on the passed in coordinates
	// menu items added to this list are automatically spaced based on the layout type
	public Menu(String title, float centerX, float centerY, float width, float height, MenuStyle style)
	{
		this.style = style;
		outline = new RoundRectangle2D.Float(0, 0, 0, 0, width/4, height/4);
		outline.setFrameFromCenter(centerX, centerY, centerX+width/2, centerY+height/2);
		items = new LinkedList<MenuItem>();
		
		// check if this menu was given a title
		if (title.compareTo("") != 0)
		{
			// this menu has a title, add it as the first menu item
			TextItem menuTitle = new TextItem(title, 32, false, 0, 0);
			add(menuTitle);
		}
	}
	
	// draw the menu on the screen
	public void draw(Graphics2D g2d)
	{
		g2d.setColor(style.colors.fillColor);
		g2d.fill(outline);
		g2d.setColor(style.colors.trimColor);
		g2d.setStroke(style.stroke);
		g2d.draw(outline);
		
		// draw each item contained in the menu
		for (MenuItem item : items)
		{
			item.draw(g2d, style);
		}
	}
	
	// add an item to the menu
	public void add(MenuItem item)
	{
		float itemX, itemY, itemYInc, itemXInc;
		float itemWidth, itemHeight;
		items.add(item);
		
		// item placement depends on the layout of the menu
		if (style.layout == LayoutType.LAYOUT_HORIZONTAL)
		{
			itemXInc = getWidth() / items.size();
			itemX = getXPos();
			itemY = getYPos() + getHeight()/2;
			for (MenuItem it : items)
			{
				itemWidth = it.getWidth();
				itemHeight = it.getHeight();
				it.outline.setRoundRect(itemX, itemY-(itemHeight/2), itemWidth, itemHeight, 
										itemWidth/4, itemHeight/4);
				itemX += itemXInc;
			}
		}
		else
		{
			// vertical layout
			itemX = getXPos() + getWidth()/2;
			itemYInc = getHeight() / (items.size()+1);
			itemY = getYPos() + itemYInc;
			for (MenuItem it : items)
			{
				itemWidth = it.getWidth();
				itemHeight = it.getHeight();
				it.outline.setRoundRect(itemX-(itemWidth/2), itemY-(itemHeight/2), itemWidth, itemHeight, 
										itemWidth/4, itemHeight/4);
				itemY += itemYInc;
			}
		}
	}
	
	// check if the menu contains the passed in coordinates
	public boolean contains(float x, float y)
	{
		return (outline.contains(x, y));
	}
	public boolean contains(Point pos)
	{
		return (outline.contains(pos));
	}
	
	// if the mouse is currently hovering over a menu item
	// then highlight it
	public void updateMousePos(Point mPos)
	{
		for (MenuItem item : items)
		{
			if (item.contains(mPos) == true)
			{
				item.setHovered(true);
			}
			else
			{
				item.setHovered(false);
			}
		}
	}
	
	// remove the highlighting from all items in the menu
	public void clearHovered()
	{
		for (MenuItem item : items)
		{
			item.setHovered(false);
		}
	}
	
	// determine if a button was clicked and if so,
	// return that button's action
	public MenuAction handleClick(Point mPos)
	{
		MenuAction action;
		
		for (MenuItem item : items)
		{
			if (item.contains(mPos))
			{
				action = item.getAction();
				
				// non-clickable menuItems have INVALID_ACTION
				if (action != MenuAction.INVALID_ACTION)
				{
					return (action);
				}
			}
		}
		return (MenuAction.INVALID_ACTION);
	}
	
	// draw a string to the screen, centered around the passed in coordinates
	public static void drawText(Graphics2D g2d, String text, int size, Color color, float centerX, float centerY)
	{
		TextLayout layout;
		Rectangle2D textBounds;
		Font textFont;
		Color textColor;
		
		// default font
		textFont = new Font("Courier", Font.BOLD, size);
		
		textColor = color;
		layout = new TextLayout(text, textFont, g2d.getFontRenderContext());
		textBounds = layout.getBounds();
		g2d.setColor(textColor);
		layout.draw(g2d, (float)(centerX-(textBounds.getWidth()/2)), 
					(float)(centerY+(layout.getAscent()/3)));
	}
}









