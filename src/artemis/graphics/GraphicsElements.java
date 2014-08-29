package artemis.graphics;

import artemis.graphics.settings.GraphicsSettings;
import artemis.graphics.settings.GraphicStyle;
import artemis.enums.ShipType;

import processing.core.PApplet;
import processing.core.PImage;

import net.dhleong.acl.enums.ObjectType;
import net.dhleong.acl.util.BoolState;
import net.dhleong.acl.vesseldata.BeamPort;
import net.dhleong.acl.vesseldata.Vessel;
import net.dhleong.acl.world.ArtemisBase;
import net.dhleong.acl.world.ArtemisDrone;
import net.dhleong.acl.world.ArtemisNebula;
import net.dhleong.acl.world.ArtemisNpc;
import net.dhleong.acl.world.ArtemisObject;
import net.dhleong.acl.world.ArtemisPlayer;
import net.dhleong.acl.world.ArtemisWhale;
import net.dhleong.acl.world.BaseArtemisShip;

public class GraphicsElements
{
	private PApplet g;
	private GraphicsSettings gs;
	private float scale;
	private GraphicStyle stWhale, stMonster, stNebula, stAsteroid,
						 stMine, stTorpedo, stBlackHole, stDrone,
						 stAnomaly;

	public GraphicsElements(PApplet g, GraphicsSettings gs, float scale)
	{
		this.g = g;
		this.gs = gs;
		this.scale = scale;
		stWhale = gs.fromObjectType(ObjectType.WHALE);
		stMonster = gs.fromObjectType(ObjectType.MONSTER);
		stNebula = gs.fromObjectType(ObjectType.NEBULA);
		stAsteroid = gs.fromObjectType(ObjectType.ASTEROID);
		stMine = gs.fromObjectType(ObjectType.MINE);
		stTorpedo = gs.fromObjectType(ObjectType.TORPEDO);
		stBlackHole = gs.fromObjectType(ObjectType.BLACK_HOLE);
		stDrone = gs.fromObjectType(ObjectType.DRONE);
		stAnomaly = gs.fromObjectType(ObjectType.ANOMALY);
	}

	public void setScale(float scale)
	{
		this.scale = scale;
	}

	private void drawArcs(Vessel v)
	{
		for (BeamPort bp : v.getBeamPorts())
		{
			g.pushMatrix();
			float theta = g.atan2(bp.getX(), bp.getZ());
			float arcwidth = bp.getArcWidth() * g.TWO_PI;
			int range = bp.getRange();
			g.arc(0, 0, range, range, -g.HALF_PI - theta - arcwidth / 2,
									  -g.HALF_PI - theta + arcwidth / 2);
			g.pushMatrix();
			g.rotate(-theta + arcwidth / 2);
			g.line(0, 0, 0, range * -1.2f);
			g.popMatrix();
			g.rotate(-theta - arcwidth / 2);
			g.line(0, 0, 0, range * -1.2f);
			g.popMatrix();
		}
	}

	private void drawShip(GraphicStyle ss, BaseArtemisShip obj, PImage img,
						  float dist, float bearing)
	{
		float heading = obj.getHeading();
		g.pushStyle();
		g.rotate(g.PI - heading);
		g.noSmooth();
		if (ss.showHeading)
		{
			g.stroke(0, 0, 170);
			g.line(0, 0, 0, -200 / scale);
		}
		Vessel v = obj.getVessel();
		if (v != null && ss.showArcs)
		{
			g.noFill();
			g.stroke(26, 196, 176);
			drawArcs(v);
		}
		g.smooth();

		g.scale(-1 / scale, 1 / scale);
		float imgHeight = gs.getShipHeight();
		float imgWidth = imgHeight * img.width / img.height;
		g.tint(ss.fillColor);
		g.image(img, 0, 0, imgWidth, imgHeight);
		g.rotate(g.PI - heading);

		g.textSize(18);
		g.fill(ss.textColor);
		if (ss.showName)
			g.text(obj.getName(), 0, -40);
		if (ss.showBearing && ss.showDist)
		{
			g.text(String.format("%03.0f", g.degrees(bearing > 0 ? bearing : g.TWO_PI + bearing)), -30, 38);
			g.text(String.format("%4.0f", dist), 30, 38);
		}
		else if (ss.showBearing)
			g.text(String.format("%03.0f", g.degrees(bearing > 0 ? bearing : g.TWO_PI + bearing)), 0, 38);
		else if (ss.showDist)
			g.text(String.format("%4.0f", dist), 0, 38);

		g.popStyle();
	}

	private GraphicStyle getStyle(BaseArtemisShip obj)
	{
		if (obj instanceof ArtemisNpc)
		{
			ArtemisNpc o = (ArtemisNpc)obj;
			if (!o.isScanned(ArtemisNpc.SCAN_LEVEL_BASIC))
				return gs.getUnscanned();
			if (o.isSurrendered() == BoolState.TRUE)
				return gs.getSurrendered();
		}
		Vessel v = obj.getVessel();
		if (v == null)
			return gs.getUnscanned();

		return gs.fromShipFaction(v.getSide());
	}

	private GraphicStyle getStyle(ArtemisBase obj)
	{
		Vessel v = obj.getVessel();
		if (v == null)
			return gs.getDefaultBase();

		return gs.fromBaseFaction(v.getSide());
	}

	public void drawShip(boolean own, BaseArtemisShip obj, PImage img,
						 float dist, float bearing)
	{
		g.pushMatrix();
		if (!own)
			g.translate(obj.getX(), obj.getZ());

		GraphicStyle st;
		if (own)
			st = gs.getOwn();
		else
			st = getStyle(obj);

		drawShip(st, obj, img, dist, bearing);
		g.popMatrix();
	}

	public void drawWhale(ArtemisWhale obj, PImage img, float dist)
	{
		GraphicStyle st = stWhale;
		g.pushMatrix();
		g.pushStyle();
		g.translate(obj.getX(), obj.getZ());
		float heading = obj.getHeading();
		g.rotate(g.PI - heading);

		g.scale(-1 / scale, 1 / scale);
		g.tint(st.fillColor);
		g.image(img, 0, 0, st.width, st.height);
		g.rotate(g.PI - heading);

		g.fill(st.textColor);
		g.textSize(st.textSize);
		if (st.showName)
			g.text(obj.getName(), 0, -35);
		if (st.showDist)
			g.text(String.format("%4.0f", dist), 0, 30);

		g.popMatrix();
		g.popStyle();
	}

	public void drawDrone(ArtemisDrone obj, PImage img, float dist)
	{
		GraphicStyle st = stDrone;
		g.pushMatrix();
		g.pushStyle();
		g.translate(obj.getX(), obj.getZ());
		float heading = obj.getHeading();
		g.rotate(g.PI - heading);

		g.scale(-1 / scale, 1 / scale);
		g.tint(st.fillColor);
		g.image(img, 0, 0, st.width, st.height);
		g.rotate(g.PI - heading);

		g.fill(st.textColor);
		g.textSize(st.textSize);
		if (st.showName)
			g.text(obj.getName(), 0, -35);
		if (st.showDist)
			g.text(String.format("%4.0f", dist), 0, 30);

		g.popMatrix();
		g.popStyle();
	}

	public void drawMonster(ArtemisObject obj, PImage img, float dist)
	{
		GraphicStyle st = stMonster;
		g.pushMatrix();
		g.pushStyle();
		g.translate(obj.getX(), obj.getZ());
		g.scale(-1 / scale, 1 / scale);

		g.tint(st.fillColor);
		g.image(img, 0, 0, st.width, st.height);

		g.fill(st.textColor);
		g.textSize(st.textSize);
		if (st.showName)
			g.text(obj.getName(), 0, -25);
		if (st.showDist)
			g.text(String.format("%.0f", dist), 0, 20);

		g.popStyle();
		g.popMatrix();
	}

	public void drawBase(ArtemisBase obj, PImage img, float dist)
	{
		GraphicStyle st = getStyle(obj);
		g.pushMatrix();
		g.pushStyle();
		g.translate(obj.getX(), obj.getZ());
		g.scale(-1 / scale, 1 / scale);
		g.tint(st.fillColor);
		g.image(img, 0, 0, st.width, st.height);
		g.fill(st.textColor);
		g.textSize(st.textSize);
		if (st.showName)
			g.text(obj.getName(), 0, -25);
		if (st.showDist)
			g.text(String.format("%.0f", dist), 0, 20);
		g.popStyle();
		g.popMatrix();
	}

	public void drawNebula(ArtemisNebula obj, PImage img)
	{
		GraphicStyle st = stNebula;
		if (obj.hasColor() && !st.ignoreColor)
			g.tint(obj.getRed(), obj.getGreen(), obj.getBlue(), st.alpha);
		else
			g.tint(st.fillColor);
		g.image(img, obj.getX(), obj.getZ(), st.width, st.height);
	}

	public void drawMine(ArtemisObject obj, PImage img)
	{
		GraphicStyle st = stMine;
		g.tint(st.fillColor);
		g.image(img, obj.getX(), obj.getZ(), st.width / scale,
											st.height / scale);
	}

	public void drawAnomaly(ArtemisObject obj)
	{
		GraphicStyle st = stAnomaly;
		if (g.millis() % (st.rate * 2) < st.rate)
		{
			g.stroke(st.fillColor);
			g.fill(st.fillColor);
			g.ellipse(obj.getX(), obj.getZ(), st.width / scale,
											  st.height / scale);
		}
	}

	public void drawTorpedo(ArtemisObject obj)
	{
		GraphicStyle st = stTorpedo;
		g.stroke(st.fillColor);
		g.fill(st.fillColor);
		g.ellipse(obj.getX(), obj.getZ(), st.width / scale,
										 st.height / scale);
	}

	public void drawAsteroid(ArtemisObject obj, PImage img)
	{
		GraphicStyle st = stAsteroid;
		g.pushMatrix();
		g.translate(obj.getX(), obj.getZ());
		g.rotate(g.radians(obj.getY()));
		g.scale(1, -1);
		g.tint(st.fillColor);
		g.image(img, 0, 0, st.width, st.height);
		g.popMatrix();
	}

	public void drawBlackHole(ArtemisObject obj, PImage img)
	{
		GraphicStyle st = stBlackHole;
		g.pushMatrix();
		g.translate(obj.getX(), obj.getZ());
		int num = g.millis() % 54000;
		g.pushMatrix();
		g.rotate(-g.radians(num / 150f));
		g.scale(1, -1);
		g.tint(st.fillColor);
		g.image(img, 0, 0, st.width, st.height);
		g.popMatrix();
		g.rotate(g.radians(num / 150f));
		g.scale(0.5f, -0.5f);
		g.image(img, 0, 0, st.width, st.height);
		g.popMatrix();
	}
}
