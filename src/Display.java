import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static java.lang.Math.atan2;

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

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Arc2D;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.LinkedList;

public class Display
{
	private JFrame frame;
	private DisplayPanel panel;
	private ArtemisNetworkInterface server;
	private SystemManager sm;
	private Image ship, radar, mine;
	private double scale = 0.1;

	public Display(String host, int port) throws IOException
	{
		server = new ThreadedArtemisNetworkInterface(host, port);
		sm = new SystemManager();
		server.addListener(sm);
		server.addListener(this);
		server.start();

		loadImages();

		frame = new JFrame("Game Window");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		panel = new DisplayPanel();
		panel.setPreferredSize(new Dimension(800, 600));

		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
	}

	@Listener
	public void onPacket(ArtemisPacket pkt)
	{
		if (panel != null)
			panel.repaint();
	}

	@Listener
	public void onConnectSuccess(ConnectionSuccessEvent event)
	{
		server.send(new ReadyPacket());
	}

	private void loadImages()
	{
		ship = null;
		radar = null;
		try
		{
			ship = ImageIO.read(new File("images/clientShipIcon.png"));
			radar = ImageIO.read(new File("images/radar2.png"));
			mine = ImageIO.read(new File("images/icon-mine.png"));
		}
		catch (IOException e)
		{
			System.out.println("Image failed to load.");
		}
	}

	private class DisplayPanel extends JPanel
	{
		private LinkedList<AffineTransform> stack;
		private Graphics2D g;

		public DisplayPanel()
		{
			stack = new LinkedList<AffineTransform>();
			g = null;
		}

		private void pushTransform()
		{
			stack.push(g.getTransform());
		}

		private void popTransform()
		{
			g.setTransform(stack.pop());
		}

		private void drawCenteredImage(Image image)
		{
			pushTransform();
			g.translate(-image.getWidth(null) / 2.0, -image.getHeight(null) / 2.0);
			g.drawImage(image, 0, 0, null);
			popTransform();
		}

		private void drawImage(double angle, double x, double y, double scale, Image image)
		{
			pushTransform();
			g.translate(x, y);
			g.rotate(angle);
			g.scale(scale, scale);
			drawCenteredImage(image);
			popTransform();
		}

		private void drawArcs(Vessel v)
		{
			for (BeamPort bp : v.getBeamPorts())
			{
				pushTransform();
				double theta = atan2(bp.getX(), bp.getZ());
				double arcwidth = bp.getArcWidth() * 2 * Math.PI;
				int range = bp.getRange();
				pushTransform();
				g.rotate(-theta + arcwidth / 2);
				g.drawLine(0, 0, 0, (int)(range * -1.2));
				g.draw(new Arc2D.Double(-range, -range, range * 2, range * 2, 90, arcwidth * 180 / Math.PI, Arc2D.OPEN));
				popTransform();
				g.rotate(-theta - arcwidth / 2);
				g.drawLine(0, 0, 0, (int)(range * -1.2));
				popTransform();
			}
		}

		@Override
		public void paintComponent(Graphics gr)
		{
			super.paintComponent(gr);
			g = (Graphics2D)gr;
			g.setBackground(Color.BLACK);
			g.clearRect(0, 0, getWidth(), getHeight());

			ArtemisPlayer p = sm.getPlayerShip(0);
			if (p != null)
			{
				double heading = p.getHeading();
				g.translate(getWidth() / 2.0, getHeight() / 2.0);
				drawCenteredImage(radar);
				g.scale(scale, scale);
				pushTransform();
				g.rotate(Math.PI + heading);
				pushTransform();
				g.scale(0.2 / scale, 0.2 / scale);
				drawCenteredImage(ship);
				popTransform();
				Vessel v = p.getVessel();
				g.setColor(Color.RED);
				drawArcs(v);
				popTransform();
				g.translate(p.getX(), -p.getZ());
				List<ArtemisObject> objs;
				objs = sm.getObjects(ObjectType.MINE);
				for (ArtemisObject obj : objs)
				{
					pushTransform();
					g.translate(-obj.getX(), obj.getZ());
					drawCenteredImage(mine);
					popTransform();
				}
			}
		}
	}
}
