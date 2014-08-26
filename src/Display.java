import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import net.dhleong.acl.util.BoolState;
import net.dhleong.acl.enums.ConnectionType;
import net.dhleong.acl.enums.ObjectType;
import net.dhleong.acl.iface.ArtemisNetworkInterface;
import net.dhleong.acl.iface.ConnectionSuccessEvent;
import net.dhleong.acl.iface.DisconnectEvent;
import net.dhleong.acl.iface.Listener;
import net.dhleong.acl.iface.ThreadedArtemisNetworkInterface;
import net.dhleong.acl.protocol.core.setup.ReadyPacket;
import net.dhleong.acl.protocol.core.world.*;
import net.dhleong.acl.protocol.ArtemisPacket;
import net.dhleong.acl.vesseldata.Vessel;
import net.dhleong.acl.vesseldata.BeamPort;
import net.dhleong.acl.world.ArtemisBase;
import net.dhleong.acl.world.ArtemisNpc;
import net.dhleong.acl.world.ArtemisObject;
import net.dhleong.acl.world.ArtemisPlayer;
import net.dhleong.acl.world.BaseArtemisShip;
import net.dhleong.acl.world.SystemManager;

import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;

import java.awt.Dimension;

import processing.core.*;

public class Display extends PApplet
{
	private ArtemisNetworkInterface server;
	private SystemManager sm;
	private PImage imUnknown, imRadar, imMine, imAsteroid, imBase;
	private HashMap<String, PImage> imShipMap;
	private float sc = 0.1f;

	public Display(String host, int port)
	{
		try
		{
			server = new ThreadedArtemisNetworkInterface(host, port);
			sm = new SystemManager();
			server.addListener(sm);
			server.addListener(this);
			server.start();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			exit();
		}
		setPreferredSize(new Dimension(1200, 800));
	}

	public void setup()
	{
		size(1200, 800);
		if (frame != null)
			frame.setResizable(true);
		String host = "localhost";
		int port = 2010;

		loadImages();
		ellipseMode(RADIUS);
		imageMode(CENTER);
		background(0);
		frameRate(30);
		noSmooth();
	}

	@Listener
	public void onConnectSuccess(ConnectionSuccessEvent event)
	{
		server.send(new ReadyPacket());
	}

	private void loadImages()
	{
		imUnknown = loadImage("images/clientShipIcon.png");
		imRadar = loadImage("images/radar2.png");
		imMine = loadImage("images/icon-mine.png");
		imAsteroid = loadImage("images/icon-asteroid.png");
		imBase = loadImage("images/stationIcon.png");
		imShipMap = new HashMap<String, PImage>();
	}

	private void drawImage(float angle, float x, float y, float sc, PImage image)
	{
		pushMatrix();
		translate(x, y);
		rotate(angle);
		scale(sc, sc);
		image(image, 0, 0);
		popMatrix();
	}

	private void drawArcs(Vessel v)
	{
		for (BeamPort bp : v.getBeamPorts())
		{
			pushMatrix();
			float theta = atan2(bp.getX(), bp.getZ());
			float arcwidth = bp.getArcWidth() * TWO_PI;
			int range = bp.getRange();
			arc(0, 0, range, range, -HALF_PI - theta - arcwidth / 2,
									-HALF_PI - theta + arcwidth / 2);
			pushMatrix();
			rotate(-theta + arcwidth / 2);
			line(0, 0, 0, range * -1.2f);
			popMatrix();
			rotate(-theta - arcwidth / 2);
			line(0, 0, 0, range * -1.2f);
			popMatrix();
		}
	}

	private PImage getShipIcon(Vessel v)
	{
		if (v == null)
			return imUnknown;
		String meshfile = v.getMeshFile();
		if (imShipMap.containsKey(meshfile))
			return imShipMap.get(meshfile);
		PImage img = loadImage(meshfile.replace(".dxs", "-icon.png"));
		println(meshfile.replace(".dxs", "-icon.png"));
		imShipMap.put(meshfile, img);
		return img;
	}

	private void drawShip(BaseArtemisShip obj, float dist, float bearing, boolean ownShip)
	{
		pushMatrix();
		noSmooth();
		if (!ownShip)
			translate(obj.getX(), obj.getZ());
		float heading = obj.getHeading();
		rotate(PI - heading);
		if (ownShip)
		{
			stroke(0, 0, 170);
			line(0, 0, 0, -200 / sc);
		}
		Vessel v = obj.getVessel();
		if (obj.getType() == ObjectType.PLAYER_SHIP)
		{
			if (v != null)
			{
				stroke(26, 196, 176);
				noFill();
				drawArcs(v);
			}
			tint(242, 252, 43);
			fill(242, 252, 43);
		}
		else
		{
			ArtemisNpc o = (ArtemisNpc)obj;
			if (o.isSurrendered() != BoolState.TRUE)
			{
				if (o.isEnemy() == BoolState.FALSE)
				{
					if (v != null)
					{
						stroke(26, 196, 176);
						noFill();
						drawArcs(v);
					}
					tint(106, 226, 252);
					fill(106, 226, 252);
				}
				else
				{
					if (v != null)
					{
						stroke(26, 196, 176);
						noFill();
						drawArcs(v);
					}
					tint(255, 0, 0);
					fill(255, 0, 0);
				}
			}
			else
			{
				tint(200, 209, 25);
				fill(200, 209, 25);
			}
		}
		smooth();

		scale(-1 / sc, 1 / sc);
		PImage img = getShipIcon(v);
		float imgHeight = 40.5f;
		float imgWidth = imgHeight * img.width / img.height;
		image(img, 0, 0, imgWidth, imgHeight);
		noTint();
		if (!ownShip)
		{
			rotate(PI - heading);
			text(obj.getName(), 0, -25);
			text(String.format("%03.0f", degrees(bearing > 0 ? bearing : TWO_PI + bearing)), -30, 20);
			text(String.format("%4.0f", dist), 30, 20);
		}

		popMatrix();
	}

	public void draw()
	{
		background(0);

		fill(255);
		textAlign(LEFT, BOTTOM);
		text(String.format("%3.1f", frameRate), 10, height - 10);
		textAlign(CENTER, CENTER);
		ArtemisPlayer p = sm.getPlayerShip(0);
		if (p == null)
			return;

		float px = p.getX(), py = p.getZ();
		// println(String.format("%.1f %.1f %.1f", p.getX(), p.getY(), p.getZ()));
		translate(width / 2f, height / 2f);
		scale(-sc, sc);
		translate(-px, -py);
		noFill();
		stroke(0, 0, 255);
		for (int i = 0; i < 6; i++)
		{
			line(0, i * 20000, 100000, i * 20000);
			line(i * 20000, 0, i * 20000, 100000);
		}
		stroke(100);

		// move origin to player ship
		pushMatrix();
		translate(px, py);
		ellipse(0, 0, 5000, 5000);

		// draw radar
		pushMatrix();
		scale(-1/sc, 1/sc);
		image(imRadar, 0, 0);
		popMatrix();

		drawShip(p, 0, 0, true);

		/*
		float heading = p.getHeading();
		Vessel v = p.getVessel();
		pushMatrix();
		rotate(PI - heading);
		stroke(0, 0, 170);
		line(0, 0, 0, -200 / sc);
		pushMatrix();
		scale(0.2f / sc, 0.2f / sc);
		smooth();
		tint(242, 252, 43);
		image(getShipIcon(v), 0, 0);
		noSmooth();
		popMatrix();
		stroke(255, 0, 0);
		noFill();
		// noSmooth();
		drawArcs(v);
		// smooth();
		popMatrix();
		*/

		// return origin
		popMatrix();
		List<ArtemisObject> objs = new LinkedList<ArtemisObject>();
		sm.getAll(objs);
		smooth();
		for (ArtemisObject obj : objs)
		{
			switch (obj.getType())
			{
				case MINE:
					image(imMine, obj.getX(), obj.getZ(), 12.8f / sc, 12.8f / sc);
					break;
				case ASTEROID:
					pushMatrix();
					translate(obj.getX(), obj.getZ());
					rotate(radians(obj.getY()));
					scale(1, -1);
					tint(254, 149, 80);
					image(imAsteroid, 0, 0, 350, 350);
					noTint();
					popMatrix();
					break;
				case BASE:
					ArtemisBase o = (ArtemisBase)obj;
					pushMatrix();
					translate(o.getX(), o.getZ());
					scale(-1 / sc, 1 / sc);
					tint(242, 252, 43);
					image(imBase, 0, 0, 25, 25);
					fill(242, 252, 43);
					text(o.getName(), 0, -20);
					text(String.format("%.0f", dist(px, py, o.getX(), o.getZ())), 0, 20);
					noTint();
					popMatrix();
					break;
				case NPC_SHIP:
					drawShip((ArtemisNpc)obj,
							 dist(px, py, obj.getX(), obj.getZ()),
							 -atan2(obj.getX() - px, py - obj.getZ()), false);
					break;
			}
		}
		noSmooth();
	}

	public void keyPressed()
	{
		if (key == 'l')
		{
			List<ArtemisObject> objs = new LinkedList<ArtemisObject>();
			sm.getAll(objs);
			for (ArtemisObject obj: objs)
			{
				if (obj.getType() == ObjectType.MINE ||
					obj.getType() == ObjectType.ASTEROID ||
					obj.getType() == ObjectType.BASE ||
					obj.getType() == ObjectType.NPC_SHIP)
					continue;
				System.out.printf("%20s %s\n", obj.getType(), obj.getClass());
			}
		}
		if (key == 'a')
		{
			ArtemisObject a = sm.getObjects(ObjectType.ASTEROID).get(0);
			System.out.println(a);
		}
		if (key == ESC)
		{
			key = 0;
		}
	}
}
