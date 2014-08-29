package artemis.graphics;

import artemis.Display;
import artemis.enums.ShipType;
import artemis.graphics.GraphicsElements;
import artemis.graphics.settings.GraphicsSettings;

import java.io.IOException;

import net.dhleong.acl.util.BoolState;
import net.dhleong.acl.enums.ObjectType;
import net.dhleong.acl.vesseldata.Vessel;
import net.dhleong.acl.world.ArtemisBase;
import net.dhleong.acl.world.ArtemisNebula;
import net.dhleong.acl.world.ArtemisNpc;
import net.dhleong.acl.world.ArtemisObject;
import net.dhleong.acl.world.ArtemisPlayer;
import net.dhleong.acl.world.ArtemisWhale;
import net.dhleong.acl.world.BaseArtemisShip;
import net.dhleong.acl.world.SystemManager;

import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;

import processing.core.PApplet;
import processing.core.PImage;

public class GraphicsHandler
{
	private Display d;
	private SystemManager sm;
	private ArtemisPlayer pl;
	private GraphicsSettings gs;
	private GraphicsElements ge;
	private PImage imUnknown, imRadar, imMine, imAsteroid, imBase,
				   imSciTarget, imWeapTarget, imNebula;
	private HashMap<String, PImage> imShipMap;
	private float sc = 0.1f;

	public GraphicsHandler(Display d, String filename)
	{
		this.d = d;
		sm = d.getSystemManager();
		pl = null;
		loadImages();
		gs = new GraphicsSettings(d.loadXML(filename));
		ge = new GraphicsElements(d, gs, sc);
	}

	private void loadImages()
	{
		imUnknown = d.loadImage("images/clientShipIcon.png");
		imRadar = d.loadImage("images/radar2.png");
		imMine = d.loadImage("images/icon-mine.png");
		imAsteroid = d.loadImage("images/icon-asteroid.png");
		imBase = d.loadImage("images/stationIcon.png");
		imSciTarget = d.loadImage("images/scienceReticle.png");
		imWeapTarget = d.loadImage("images/targetReticle2.png");
		imNebula = d.loadImage("images/icon-nebula.png");
		imShipMap = new HashMap<String, PImage>();
	}

	private float getScale()
	{
		return sc;
	}

	private void setScale(float scale)
	{
		sc = scale;
		ge.setScale(scale);
	}

	private PImage getShipIcon(BaseArtemisShip s)
	{
		if (s == null)
			return imUnknown;
		if (s instanceof ArtemisNpc && ((ArtemisNpc)s).getScanLevel() <= 0)
			return imUnknown;
		Vessel v = s.getVessel();
		if (v == null)
			return imUnknown;
		String meshfile = v.getMeshFile();
		if (imShipMap.containsKey(meshfile))
			return imShipMap.get(meshfile);
		PImage img = d.loadImage(meshfile.replace(".dxs", "-icon.png"));
		imShipMap.put(meshfile, img);
		return img;
	}

	private void drawShip(BaseArtemisShip obj, float dist, float bearing, boolean ownShip)
	{
		PImage img = getShipIcon(obj);

		ge.drawShip(ownShip, obj, img, dist, bearing);
	}

	private void drawReticule(ArtemisObject obj, boolean sciTarget,
							  boolean capTarget, boolean weapTarget)
	{
		d.pushMatrix();
		d.translate(obj.getX(), obj.getZ());
		d.scale(-1 / sc, 1 / sc);
		if (sciTarget)
		{
			d.tint(29, 161, 55);
			float size = 64;
			int num = (d.millis() % 500) / 25;
			if (num > 10)
				num = 20 - num;
			size += num;
			d.image(imSciTarget, 0, 0, size, size);
			d.noTint();
		}
		if (capTarget)
		{
			d.tint(128, 235, 61);
			int num = (d.millis() % 3600) / 10;
			d.pushMatrix();
			d.rotate(-d.radians(num));
			d.image(imSciTarget, 0, 0, 64, 64);
			d.popMatrix();
			d.noTint();
		}
		if (weapTarget)
		{
			d.tint(255, 0, 0);
			int num = (d.millis() % 7200) / 20;
			d.pushMatrix();
			d.rotate(d.radians(num));
			d.image(imWeapTarget, 0, 0, 64, 64);
			d.popMatrix();
			d.noTint();
		}
		d.popMatrix();
	}

	private void drawObject(ArtemisObject obj, float dist)
	{
		switch (obj.getType())
		{
			case MINE:
				ge.drawMine(obj, imMine);
				break;
			case ANOMALY:
				ge.drawAnomaly(obj);
				break;
			case TORPEDO:
				ge.drawTorpedo(obj);
				break;
			case ASTEROID:
				ge.drawAsteroid(obj, imAsteroid);
				break;
			case BASE:
				ge.drawBase((ArtemisBase)obj, imBase, dist);
				break;
			case WHALE:
				ge.drawWhale((ArtemisWhale)obj, imUnknown, dist);
				break;
			case MONSTER:
				ge.drawMonster(obj, imBase, dist);
				break;
			case NPC_SHIP:
			case PLAYER_SHIP:
				drawShip((ArtemisNpc)obj, dist,
						 -d.atan2(obj.getX() - pl.getX(),
						 		  pl.getZ() - obj.getZ()),
						 false);
				break;
		}
	}

	public void draw()
	{
		d.background(0);

		d.fill(255);
		d.textAlign(d.LEFT, d.BOTTOM);
		d.text(String.format("%3.1f", d.frameRate), 10, d.height - 10);
		d.textAlign(d.CENTER, d.CENTER);
		if (pl == null)
			pl = sm.getPlayerShip(0);
		if (pl == null)
			return;

		float px = pl.getX(), pz = pl.getZ();
		d.translate(d.width / 2f, d.height / 2f);
		d.scale(-sc, sc);
		d.translate(-px, -pz);
		d.stroke(0, 0, 255);
		for (int i = 0; i < 6; i++)
		{
			d.line(0, i * 20000, 100000, i * 20000);
			d.line(i * 20000, 0, i * 20000, 100000);
		}

		List<ArtemisObject> objs = new LinkedList<ArtemisObject>();
		// draw nebulas
		objs = sm.getObjects(ObjectType.NEBULA);
		d.pushStyle();
		d.smooth();
		float dist;
		for (ArtemisObject obj : objs)
		{
			dist = d.dist(px, pz, obj.getX(), obj.getZ());
			if (dist > d.width / sc)
				continue;
			ge.drawNebula((ArtemisNebula)obj, imNebula);
		}
		d.popStyle();

		// move origin to player ship
		d.pushMatrix();
		d.translate(px, pz);
		d.stroke(100);
		d.noFill();
		d.ellipse(0, 0, 5000, 5000);

		// draw radar
		d.pushMatrix();
		d.scale(-1 / sc, 1 / sc);
		d.image(imRadar, 0, 0);
		d.popMatrix();

		drawShip(pl, 0, 0, true);

		// return origin
		d.popMatrix();
		objs.clear();
		sm.getAll(objs);
		d.smooth();
		d.pushStyle();
		for (ArtemisObject obj : objs)
		{
			dist = d.dist(px, pz, obj.getX(), obj.getZ());
			if (dist > d.width / sc)
				continue;
			boolean sciTarget = obj.getId() == pl.getScienceTarget(),
					capTarget = obj.getId() == pl.getCaptainTarget(),
					weaTarget = obj.getId() == pl.getWeaponsTarget();
			if (sciTarget || capTarget || weaTarget)
				drawReticule(obj, sciTarget, capTarget, weaTarget);
			if (obj == pl)
				continue;
			drawObject(obj, dist);
		}
		d.popStyle();
		d.noSmooth();
	}
}
