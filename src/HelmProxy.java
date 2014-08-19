import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import net.dhleong.acl.enums.Console;
import net.dhleong.acl.enums.ConnectionType;
import net.dhleong.acl.enums.ShipSystem;
import net.dhleong.acl.enums.MainScreenView;
import net.dhleong.acl.iface.ArtemisNetworkInterface;
import net.dhleong.acl.iface.ConnectionSuccessEvent;
import net.dhleong.acl.iface.DisconnectEvent;
import net.dhleong.acl.iface.Listener;
import net.dhleong.acl.iface.ThreadedArtemisNetworkInterface;
import net.dhleong.acl.protocol.ArtemisPacket;
import net.dhleong.acl.protocol.core.*;
import net.dhleong.acl.protocol.core.helm.*;
import net.dhleong.acl.protocol.core.setup.ReadyPacket;
import net.dhleong.acl.protocol.core.setup.SetConsolePacket;
import net.dhleong.acl.protocol.core.world.MainPlayerUpdatePacket;
import net.dhleong.acl.protocol.RawPacket;
import net.dhleong.acl.util.BoolState;
import net.dhleong.acl.vesseldata.VesselData;
import net.dhleong.acl.world.ArtemisPlayer;

public class HelmProxy implements Runnable
{
	private int port;
	private String serverAddr;
	private int serverPort;

	public HelmProxy(String serverAddr, int serverPort, int proxyPort) throws IOException
	{
		this.port = proxyPort;
		this.serverAddr = serverAddr;
		this.serverPort = serverPort;
	}

	public void run()
	{
		ServerSocket listener = null;
		try
		{
			listener = new ServerSocket(this.port, 0);
			listener.setSoTimeout(0);
			Socket skt = listener.accept();
			System.out.println("Client connected.");
			ThreadedArtemisNetworkInterface client = new ThreadedArtemisNetworkInterface(skt, ConnectionType.CLIENT);
			client.setParsePackets(false);
			ThreadedArtemisNetworkInterface server = new ThreadedArtemisNetworkInterface(serverAddr, serverPort);
			new ProxyListener(server, client);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (listener != null && !listener.isClosed())
			{
				try
				{
					listener.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public class ProxyListener extends Processor
	{
		private ArtemisNetworkInterface client;
		private ProxyListener(ArtemisNetworkInterface server, ArtemisNetworkInterface client) throws IOException
		{
			super(server);
			this.client = client;
			server.addListener(this);
			client.addListener(this);
			server.start();
			client.start();
		}

		@Listener
		public void onMainPlayerUpdatePacket(MainPlayerUpdatePacket pkt)
		{
			if (pkt.getObjects().size() > 1)
				System.out.println(pkt.getObjects());
			ArtemisPlayer player = (ArtemisPlayer)pkt.getObjects().get(0);

			updateCurrentState(player);
			client.send(pkt);
		}

		@Listener
		public void onDisconnect(DisconnectEvent event)
		{
			server.stop();
			client.stop();
			System.out.println("Disconnected: " + event.getCause());
		}

		@Listener
		public void onPacket(RawPacket pkt)
		{
			ConnectionType type = pkt.getConnectionType();
			ArtemisNetworkInterface dest = type == ConnectionType.SERVER ? client : server;
			dest.send(pkt);
			System.out.println(type + "> " + pkt);
		}
	}
}
