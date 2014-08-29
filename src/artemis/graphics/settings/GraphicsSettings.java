package artemis.graphics.settings;

import processing.data.XML;

import java.util.HashMap;

import net.dhleong.acl.enums.ObjectType;

public class GraphicsSettings
{
	private float shipHeight = 40.5f;
	private HashMap<String, GraphicStyle> styles;
	private HashMap<Integer, GraphicStyle> shipFactions, baseFactions;
	private HashMap<ObjectType, GraphicStyle> objectStyles;
	private GraphicStyle ownShip, surrenderedShip, unscannedShip,
						 defaultShip, defaultBase;

	public GraphicsSettings(XML tree)
	{
		styles = new HashMap<String, GraphicStyle>();
		shipFactions = new HashMap<Integer, GraphicStyle>();
		baseFactions = new HashMap<Integer, GraphicStyle>();
		objectStyles = new HashMap<ObjectType, GraphicStyle>();
		if (tree == null)
		{
			ownShip = new GraphicStyle();
			surrenderedShip = new GraphicStyle();
			unscannedShip = new GraphicStyle();
			defaultShip = new GraphicStyle();
			defaultBase = new GraphicStyle();
			return;
		}
		defaultShip = null;
		defaultBase = null;
		XML child;
		if ((child = tree.getChild("ShipSettings")) != null)
		{
			shipHeight = child.getFloat("height", 40.5f);
		}

		if ((child = tree.getChild("Styles")) != null)
		{
			for (XML style : child.getChildren())
			{
				if (style.getName().equals("#text"))
					continue;
				GraphicStyle cur = new GraphicStyle(style);
				styles.put(style.getName(), cur);
			}
		}

		if ((child = tree.getChild("Factions")) != null)
		{
			for (XML faction : child.getChildren())
			{
				if (faction.getName().equals("Faction"))
				{
					int id = faction.getInt("id", -1);
					String style;
					if (faction.hasAttribute("shipStyle"))
					{
						style = faction.getString("shipStyle");
						if (styles.containsKey(style))
							shipFactions.put(id, styles.get(style));
						else
							System.err.printf("ERROR: Style \"%s\" not found\n", style);
					}
					if (faction.hasAttribute("baseStyle"))
					{
						style = faction.getString("baseStyle");
						if (styles.containsKey(style))
							baseFactions.put(id, styles.get(style));
						else
							System.err.printf("ERROR: Style \"%s\" not found\n", style);
					}
				}
				else if (faction.getName().equals("DefaultFaction"))
				{
					String style;
					if (faction.hasAttribute("shipStyle"))
					{
						style = faction.getString("shipStyle");
						if (styles.containsKey(style))
							defaultShip = styles.get(style);
						else
							System.err.printf("WARNING: Style \"%s\" not found\n", style);
					}
					if (faction.hasAttribute("baseStyle"))
					{
						style = faction.getString("baseStyle");
						if (styles.containsKey(style))
							defaultBase = styles.get(style);
						else
							System.err.printf("WARNING: Style \"%s\" not found\n", style);
					}
				}
			}
		}

		if (defaultShip == null)
		{
			if (styles.containsKey("DefaultShip"))
			{
				System.err.println("WARNING: Default faction ship style unspecified, using \"DefaultShip\" style");
				defaultShip = styles.get("DefaultShip");
			}
			else
			{
				System.err.println("WARNING: Default faction ship style unspecified, \"DefaultShip\" style not found");
				defaultShip = new GraphicStyle();
			}
		}

		if (defaultBase == null)
		{
			if (styles.containsKey("DefaultBase"))
			{
				System.err.println("WARNING: Default faction base style unspecified, using \"DefaultBase\" style");
				defaultBase = styles.get("DefaultBase");
			}
			else
			{
				System.err.println("WARNING: Default faction base style unspecified, \"DefaultBase\" style not found");
				defaultBase = new GraphicStyle();
			}
		}

		if (styles.containsKey("OwnShip"))
		{
			ownShip = styles.get("OwnShip");
		}
		else
		{
			System.err.println("WARNING: \"OwnShip\" style not found");
			ownShip = new GraphicStyle();
		}

		if (styles.containsKey("SurrenderedShip"))
		{
			surrenderedShip = styles.get("SurrenderedShip");
		}
		else
		{
			System.err.println("WARNING: \"SurrenderedShip\" style not found");
			surrenderedShip = new GraphicStyle();
		}

		if (styles.containsKey("UnscannedShip"))
		{
			unscannedShip = styles.get("UnscannedShip");
		}
		else
		{
			System.err.println("WARNING: \"UnscannedShip\" style not found");
			unscannedShip = new GraphicStyle();
		}

		if (styles.containsKey("Whale"))
		{
			objectStyles.put(ObjectType.WHALE, styles.get("Whale"));
		}
		else
		{
			System.err.println("WARNING: \"Whale\" style not found");
			objectStyles.put(ObjectType.WHALE, new GraphicStyle());
		}

		if (styles.containsKey("Monster"))
		{
			objectStyles.put(ObjectType.MONSTER, styles.get("Monster"));
		}
		else
		{
			System.err.println("WARNING: \"Monster\" style not found");
			objectStyles.put(ObjectType.MONSTER, new GraphicStyle());
		}

		if (styles.containsKey("Nebula"))
		{
			objectStyles.put(ObjectType.NEBULA, styles.get("Nebula"));
		}
		else
		{
			System.err.println("WARNING: \"Nebula\" style not found");
			objectStyles.put(ObjectType.NEBULA, new GraphicStyle());
		}

		if (styles.containsKey("Mine"))
		{
			objectStyles.put(ObjectType.MINE, styles.get("Mine"));
		}
		else
		{
			System.err.println("WARNING: \"Mine\" style not found");
			objectStyles.put(ObjectType.MINE, new GraphicStyle());
		}

		if (styles.containsKey("Anomaly"))
		{
			objectStyles.put(ObjectType.ANOMALY, styles.get("Anomaly"));
		}
		else
		{
			System.err.println("WARNING: \"Anomaly\" style not found");
			objectStyles.put(ObjectType.ANOMALY, new GraphicStyle());
		}

		if (styles.containsKey("Torpedo"))
		{
			objectStyles.put(ObjectType.TORPEDO, styles.get("Torpedo"));
		}
		else
		{
			System.err.println("WARNING: \"Torpedo\" style not found");
			objectStyles.put(ObjectType.TORPEDO, new GraphicStyle());
		}

		if (styles.containsKey("Asteroid"))
		{
			objectStyles.put(ObjectType.ASTEROID, styles.get("Asteroid"));
		}
		else
		{
			System.err.println("WARNING: \"Asteroid\" style not found");
			objectStyles.put(ObjectType.ASTEROID, new GraphicStyle());
		}

		if (styles.containsKey("BlackHole"))
		{
			objectStyles.put(ObjectType.BLACK_HOLE, styles.get("BlackHole"));
		}
		else
		{
			System.err.println("WARNING: \"BlackHole\" style not found");
			objectStyles.put(ObjectType.BLACK_HOLE, new GraphicStyle());
		}

		if (styles.containsKey("Drone"))
		{
			objectStyles.put(ObjectType.DRONE, styles.get("Drone"));
		}
		else
		{
			System.err.println("WARNING: \"Drone\" style not found");
			objectStyles.put(ObjectType.DRONE, new GraphicStyle());
		}
	}

	public GraphicStyle fromShipFaction(int faction)
	{
		if (shipFactions.containsKey(faction))
			return shipFactions.get(faction);
		else
			return defaultShip;
	}

	public GraphicStyle fromBaseFaction(int faction)
	{
		if (baseFactions.containsKey(faction))
			return baseFactions.get(faction);
		else
			return defaultBase;
	}

	public GraphicStyle fromObjectType(ObjectType type)
	{
		if (objectStyles.containsKey(type))
		{
			return objectStyles.get(type);
		}
		else
		{
			GraphicStyle gs = new GraphicStyle();
			objectStyles.put(type, gs);
			return gs;
		}
	}

	public GraphicStyle getOwn()
	{
		return ownShip;
	}

	public GraphicStyle getSurrendered()
	{
		return surrenderedShip;
	}

	public GraphicStyle getUnscanned()
	{
		return unscannedShip;
	}

	public GraphicStyle getDefaultShip()
	{
		return defaultShip;
	}

	public GraphicStyle getDefaultBase()
	{
		return defaultBase;
	}

	public float getShipHeight()
	{
		return shipHeight;
	}
}
