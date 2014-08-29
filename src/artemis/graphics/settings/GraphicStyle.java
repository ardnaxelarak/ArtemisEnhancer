package artemis.graphics.settings;

import processing.data.XML;

public class GraphicStyle
{
	public boolean showHeading = false,
				   showBearing = true,
				   showDist = true,
				   showName = true,
				   showShields = true,
				   showArcs = true,
				   ignoreColor = false;
	public int arcColor = 0xff1ac4b0,
			   fillColor = 0xfff2fc2b,
			   textColor = 0xfff2fc2b;
	public float width = 30,
				 height = 30,
				 textSize = 18;
	public int rate = 500,
			   alpha = 150;
	public String name = "";

	public GraphicStyle()
	{
	}

	public GraphicStyle(XML tree)
	{
		if (tree == null)
			return;
		this.name = tree.getName();
		if (tree.hasAttribute("showHeading"))
			showHeading = (tree.getInt("showHeading") > 0);
		if (tree.hasAttribute("showShields"))
			showShields = tree.getInt("showShields") > 0;
		if (tree.hasAttribute("width"))
			width = tree.getFloat("width");
		if (tree.hasAttribute("height"))
			height = tree.getFloat("height");
		if (tree.hasAttribute("rate"))
			rate = tree.getInt("rate");
		XML child;
		if ((child = tree.getChild("Arcs")) != null)
		{
			if (child.hasAttribute("show"))
				showArcs = child.getInt("show") > 0;
			if ((child = child.getChild("Color")) != null)
				arcColor = color(child.getInt("r", 0), child.getInt("g", 0), child.getInt("b", 0), child.getInt("a", 255));
		}
		if ((child = tree.getChild("Fill")) != null)
		{
			if (child.hasAttribute("alpha"))
				alpha = child.getInt("alpha");
			if (child.hasAttribute("ignoreColor"))
				ignoreColor = child.getInt("ignoreColor") > 0;
			if ((child = child.getChild("Color")) != null)
				fillColor = color(child.getInt("r", 0), child.getInt("g", 0), child.getInt("b", 0), child.getInt("a", 255));
		}
		if ((child = tree.getChild("Text")) != null)
		{
			if (child.hasAttribute("size"))
				textSize = child.getFloat("size");
			if (child.hasAttribute("showBearing"))
				showBearing = child.getInt("showBearing") > 0;
			if (child.hasAttribute("showDist"))
				showDist = child.getInt("showDist") > 0;
			if (child.hasAttribute("showName"))
				showName = child.getInt("showName") > 0;
			if ((child = child.getChild("Color")) != null)
				textColor = color(child.getInt("r", 0), child.getInt("g", 0), child.getInt("b", 0), child.getInt("a", 255));
		}
	}

	private static int color(int r, int g, int b, int a)
	{
		int color = 0;
		color |= ((a & 0xff) << 24);
		color |= ((r & 0xff) << 16);
		color |= ((g & 0xff) << 8);
		color |= (b & 0xff);
		return color;
	}
}
