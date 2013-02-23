package cs4300;

import java.awt.Color;

// a holder for a set of colors that determines the look of the game
public class ColorScheme
{
	public Color fillColor, backgroundColor, trimColor, hoverColor, selectedColor, textColor;
	
	public ColorScheme(Color fill, Color background, Color trim, Color hover, Color selected, Color text)
	{
		fillColor = fill;
		backgroundColor = background;
		trimColor = trim;
		hoverColor = hover;
		selectedColor = selected;
		textColor = text;
	}
}
