package artemis;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import net.dhleong.acl.enums.ConnectionType;
import net.dhleong.acl.iface.ArtemisNetworkInterface;
import net.dhleong.acl.iface.DisconnectEvent;
import net.dhleong.acl.iface.Listener;
import net.dhleong.acl.iface.ThreadedArtemisNetworkInterface;
import net.dhleong.acl.protocol.ArtemisPacket;
import net.dhleong.acl.protocol.RawPacket;

public class DebugProxy implements Runnable
{
	private int port;
	private String serverAddr;
	private int serverPort;
	private boolean printClient, printServer;
	private boolean parseClient, parseServer;

	public DebugProxy(String serverAddr, int serverPort, int proxyPort,
					  boolean parseClient, boolean parseServer,
					  boolean printClient, boolean printServer) throws IOException
	{
		this.port = proxyPort;
		this.serverAddr = serverAddr;
		this.serverPort = serverPort;
		this.parseClient = parseClient;
		this.parseServer = parseServer;
		this.printClient = printClient;
		this.printServer = printServer;
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
			if (!parseClient)
				client.setParsePackets(false);
			ThreadedArtemisNetworkInterface server = new ThreadedArtemisNetworkInterface(serverAddr, serverPort);
			if (!parseServer)
				server.setParsePackets(false);
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

	public class ProxyListener
	{
		private ArtemisNetworkInterface client, server;
		private ProxyListener(ArtemisNetworkInterface server, ArtemisNetworkInterface client) throws IOException
		{
			this.server = server;
			this.client = client;
			server.addListener(this);
			client.addListener(this);
			server.start();
			client.start();
		}

		@Listener
		public void onDisconnect(DisconnectEvent event)
		{
			server.stop();
			client.stop();
			System.out.println("Disconnected: " + event.getCause());
		}

		@Listener
		public void onPacket(ArtemisPacket pkt)
		{
			ConnectionType type = pkt.getConnectionType();
			ArtemisNetworkInterface dest = type == ConnectionType.SERVER ? client : server;
			dest.send(pkt);
			if ((dest == server && printClient) ||
				(dest == client && printServer))
			{
				System.out.println(pkt);
			}
		}

/*
		@Listener
		public void onPacket(RawPacket pkt)
		{
			ConnectionType type = pkt.getConnectionType();
			ArtemisNetworkInterface dest = type == ConnectionType.SERVER ? client : server;
			dest.send(pkt);
			if ((dest == server && printClient && !parseClient) ||
				(dest == client && printServer && !parseServer))
			{
				System.out.println(pkt);
			}
		}
*/
	}
}
