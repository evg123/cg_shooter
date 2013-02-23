package cs4300;

import java.awt.Graphics2D;

// a menu item used to display text
public class TextItem extends MenuItem
{
	private String text;
	private int textSize;
	private boolean hasOutline;
	
	public void setText(String newText) { text = newText; }
	
	// create a new TextItem
	// a blank text item with no outline can be used as a spacer
	public TextItem(String text, int textSize, boolean hasOutline, float width, float height)
	{
		super(width, height);
		this.hasOutline = hasOutline;
		this.text = text;
		this.textSize = textSize;
	}
	
	@Override
	public void draw(Graphics2D g2d, MenuStyle style)
	{
		// only call the superclass draw function if outline should be drawn
		if (hasOutline == true)
		{
			super.draw(g2d, style);
		}
		
		if (text.compareTo("") != 0)
		{
			// dont draw the text if there is none
			Menu.drawText(g2d, text, textSize, style.colors.textColor, (float)outline.getCenterX(), (float)outline.getCenterY());
		}
	}
	
	@Override
	// not used - text items don't respond to hover
	public void setHovered(boolean hovered)	{}
}






