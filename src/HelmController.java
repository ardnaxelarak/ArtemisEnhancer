import java.io.IOException;

import net.dhleong.acl.enums.Console;
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
import net.dhleong.acl.util.BoolState;
import net.dhleong.acl.vesseldata.VesselData;
import net.dhleong.acl.world.ArtemisPlayer;

public class HelmController extends Processor
{
	public HelmController(String host, int port) throws IOException
	{
		super(new ThreadedArtemisNetworkInterface(host, port));
		server.addListener(this);
		server.start();
		listen();
	}

	@Listener
	public void onMainPlayerUpdatePacket(MainPlayerUpdatePacket pkt)
	{
		if (pkt.getObjects().size() > 1)
			System.out.println(pkt.getObjects());
		ArtemisPlayer player = (ArtemisPlayer)pkt.getObjects().get(0);

		updateCurrentState(player);
	}

	@Listener
	public void onConnectSuccess(ConnectionSuccessEvent event)
	{
		//server.send(new SetConsolePacket(Console.HELM, true));
		server.send(new ReadyPacket());
	}

	@Listener
	public void onDisconnect(DisconnectEvent event)
	{
		System.out.println("Disconnected: " + event.getCause());
	}
}
