package cs4300;

import java.awt.BasicStroke;

// a holder for several aspects of a menu that determine its look
public class MenuStyle
{
	public enum LayoutType
	{
		LAYOUT_HORIZONTAL, LAYOUT_VERTICAL
	}
	
	public BasicStroke stroke;
	public LayoutType layout;
	public ColorScheme colors;
	
	public MenuStyle(LayoutType layout, ColorScheme colors, int outlineWidth)
	{
		if ((layout == LayoutType.LAYOUT_HORIZONTAL) || (layout == LayoutType.LAYOUT_VERTICAL))
		{
			this.layout = layout;
		}
		else
		{
			// default is vertical layout
			this.layout = LayoutType.LAYOUT_VERTICAL;
		}
		this.colors = colors;
		stroke = new BasicStroke(outlineWidth);
	}
}
