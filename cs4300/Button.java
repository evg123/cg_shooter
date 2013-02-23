package cs4300;

import java.awt.Graphics2D;

import cs4300.Menu.MenuAction;

// a type of MenuItem that can be clicked to produce an action
public class Button extends MenuItem
{
	private String text;
	
	public Button(String text, MenuAction action, float width, float height)
	{
		super(width, height);
		this.text = text;
		this.action = action;
	}
	
	@Override
	public void draw(Graphics2D g2d, MenuStyle style)
	{
		super.draw(g2d, style);
		
		// draw the text onto the button
		Menu.drawText(g2d, text, 20, style.colors.textColor, (float)outline.getCenterX(), (float)outline.getCenterY());
	}

}
