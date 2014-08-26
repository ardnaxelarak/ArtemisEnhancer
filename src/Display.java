import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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
import net.dhleong.acl.world.ArtemisObject;
import net.dhleong.acl.world.ArtemisPlayer;
import net.dhleong.acl.world.SystemManager;

import java.util.List;
import java.util.LinkedList;

import java.awt.Dimension;

import processing.core.*;

public class Display extends PApplet
{
	private ArtemisNetworkInterface server;
	private SystemManager sm;
	private PImage ship, radar, mine;
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
		ship = loadImage("images/clientShipIcon.png");
		radar = loadImage("images/radar2.png");
		mine = loadImage("images/icon-mine.png");
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

	public void draw()
	{
		background(0);

		stroke(255);
		text(String.format("%3.1f", frameRate), 10, height - 10);
		ArtemisPlayer p = sm.getPlayerShip(0);
		if (p == null)
			return;

		// println(String.format("%.1f %.1f %.1f", p.getX(), p.getY(), p.getZ()));
		translate(width / 2f, height / 2f);
		scale(-sc, sc);
		translate(-p.getX(), -p.getZ());
		noFill();
		stroke(0, 0, 255);
		for (int i = 0; i < 6; i++)
		{
			line(0, i * 20000, 100000, i * 20000);
			line(i * 20000, 0, i * 20000, 100000);
		}
		stroke(100);
		ellipse(p.getX(), p.getZ(), 5000, 5000);

		// move origin to player ship
		pushMatrix();
		translate(p.getX(), p.getZ());

		// draw radar
		pushMatrix();
		scale(-1/sc, 1/sc);
		image(radar, 0, 0);
		popMatrix();

		float heading = p.getHeading();
		pushMatrix();
		rotate(PI - heading);
		stroke(0, 0, 170);
		line(0, 0, 0, -200 / sc);
		pushMatrix();
		scale(0.2f / sc, 0.2f / sc);
		image(ship, 0, 0);
		popMatrix();
		Vessel v = p.getVessel();
		stroke(255, 0, 0);
		noFill();
		// noSmooth();
		drawArcs(v);
		// smooth();
		popMatrix();

		// return origin
		popMatrix();
		List<ArtemisObject> objs;
		objs = sm.getObjects(ObjectType.MINE);
		smooth();
		for (ArtemisObject obj : objs)
		{
			image(mine, obj.getX(), obj.getZ(), 12.8f / sc, 12.8f / sc);
		}
		noSmooth();
	}
}
