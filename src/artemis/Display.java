package artemis;

import artemis.graphics.GraphicsHandler;

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

import java.awt.Dimension;

import processing.core.*;

public class Display extends PApplet
{
	private ArtemisNetworkInterface server;
	private SystemManager sm;
	private GraphicsHandler gh;

	public Display(String host, int port)
	{
		try
		{
			server = new ThreadedArtemisNetworkInterface(host, port);
			sm = new SystemManager();
			// pl = null;
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

	public SystemManager getSystemManager()
	{
		return sm;
	}

	public void setup()
	{
		size(1200, 800);
		if (frame != null)
			frame.setResizable(true);
		String host = "localhost";
		int port = 2010;

		ellipseMode(RADIUS);
		imageMode(CENTER);
		background(0);
		frameRate(30);
		noSmooth();

		gh = new GraphicsHandler(this, "data/graphics.xml");
	}

	@Listener
	public void onConnectSuccess(ConnectionSuccessEvent event)
	{
		server.send(new ReadyPacket());
	}

	public void draw()
	{
		gh.draw();
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
					obj.getType() == ObjectType.TORPEDO ||
					obj.getType() == ObjectType.NEBULA ||
					obj.getType() == ObjectType.ANOMALY ||
					obj.getType() == ObjectType.WHALE ||
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
		/*
		if (key == 's' && pl != null)
		{
			System.out.println(pl.getScienceTarget());
			ArtemisObject a = sm.getObject(pl.getScienceTarget());
			System.out.println(a);
		}
		*/
		if (key == ESC)
		{
			key = 0;
		}
	}
}
