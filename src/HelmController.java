import java.io.IOException;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

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

public class HelmController
{
	public static void main(String[] args) throws IOException
	{
		if (args.length == 0)
		{
			System.out.println("Usage: HelmController {host} [port]");
			return;
		}

		String host = args[0];
		int port = args.length > 1 ? Integer.parseInt(args[1]) : 2010;

		new HelmController(host, port);
	}

	private ArtemisNetworkInterface server;
	private static float RUDDER_DEADZONE = 0.1f;
	private static float IMPULSE_DEADZONE = 0.1f;
	private static float CLIMBDIVE_DEADZONE = 0.1f;

	private boolean shieldsUp = false;
	private boolean shiftButton = false;
	private boolean shipReverse = false;
	private int shipWarp = 0;
	private float shipImpulse = 0f;
	private float impulseControl = 0f;
	private MainScreenView frontScreen = MainScreenView.FORE;

	public HelmController(String host, int port) throws IOException
	{
		server = new ThreadedArtemisNetworkInterface(host, port);
		server.addListener(this);
		server.start();
		// currentState = null;
		Joystick j = new Joystick("/dev/input/js0", false);
		while (true)
		{
			Event e = j.getEvent();
			if (e != null)
				process(e);
		}
	}

	@Listener
	public void onMainPlayerUpdatePacket(MainPlayerUpdatePacket pkt)
	{
		if (pkt.getObjects().size() > 1)
			System.out.println(pkt.getObjects());
		ArtemisPlayer player = (ArtemisPlayer)pkt.getObjects().get(0);

		BoolState shields = player.getShieldsState();
		if (BoolState.isKnown(shields))
			shieldsUp = shields.getBooleanValue();

		BoolState reverse = player.getReverseState();
		if (BoolState.isKnown(reverse))
			shipReverse = reverse.getBooleanValue();

		MainScreenView msv = player.getMainScreen();
		if (player.getMainScreen() != null)
			frontScreen = msv;

		int warp = player.getWarp();
		if (warp != -1)
			shipWarp = warp;

		float impulse = player.getImpulse();
		if (impulse != -1)
		{
			shipImpulse = impulse;
		}
	}

	private boolean fromBoolState(BoolState bs)
	{
		return bs == BoolState.TRUE;
	}

	private void process(Event e)
	{
		float value;
		if (e.type == 2) // axis
		{
			switch (e.number)
			{
				case 0: // rudder
					value = e.value / 32767f;
					value = value / 2 + 0.5f;
					server.send(new HelmSetSteeringPacket(value));
					break;
				case 1: // impulse
					value = e.value / -32767f;
					if (value > 0)
					{
						if (value > shipImpulse)
						{
							shipImpulse = value;
							server.send(new HelmSetImpulsePacket(value));
						}
					}
					else if (value < 0)
					{
						if (shipImpulse > 1 + value)
						{
							shipImpulse = 1 + value;
							server.send(new HelmSetImpulsePacket(1 + value));
						}
					}
					break;
				case 4:
					if (shiftButton)
					{
						if (e.value == 32767)
							server.send(new SetMainScreenPacket(MainScreenView.STARBOARD));
						else if (e.value == -32767)
							server.send(new SetMainScreenPacket(MainScreenView.PORT));
					}
					break;
				case 5:
					if (shiftButton)
					{
						if (e.value == 32767)
							server.send(new SetMainScreenPacket(MainScreenView.AFT));
						else if (e.value == -32767)
							server.send(new SetMainScreenPacket(MainScreenView.FORE));
					}
					break;
			}
		}
		else if (e.type == 1 && e.value == 1) // button pressed
		{
			switch (e.number)
			{
				case 0: // warp 1
					if (shipWarp == 1)
					{
						server.send(new HelmSetWarpPacket(0));
						shipWarp = 0;
					}
					else
					{
						if (shipReverse)
						{
							server.send(new HelmToggleReversePacket());
							shipReverse = false;
						}
						server.send(new HelmSetWarpPacket(1));
						shipWarp = 1;
					}
					break;
				case 1: // increase warp
					if (shipWarp < 4)
					{
						if (shipReverse)
						{
							server.send(new HelmToggleReversePacket());
							shipReverse = false;
						}
						server.send(new HelmSetWarpPacket(shipWarp + 1));
						shipWarp += 1;
					}
					break;
				case 4: // stop
					if (shipWarp > 0)
					{
						server.send(new HelmSetWarpPacket(0));
						shipWarp = 0;
					}
					else
					{
						server.send(new HelmSetImpulsePacket(0));
						shipImpulse = 0;
					}
					break;
				case 5: // request dock
					server.send(new HelmRequestDockPacket());
					break;
				case 6: // raise shields
					if (!shieldsUp)
					{
						server.send(new ToggleShieldsPacket());
						// System.out.println("raised shields");
						shieldsUp = true;
					}
					break;
				case 7: // lower shields
					if (shieldsUp)
					{
						server.send(new ToggleShieldsPacket());
						// System.out.println("lowered shields");
						shieldsUp = false;
					}
					break;
				case 8: // toggle front screen
					if (shiftButton && frontScreen != MainScreenView.LONG_RANGE)
						server.send(new SetMainScreenPacket(MainScreenView.LONG_RANGE));
					else if (frontScreen == MainScreenView.TACTICAL)
						server.send(new SetMainScreenPacket(MainScreenView.FORE));
					else
						server.send(new SetMainScreenPacket(MainScreenView.TACTICAL));
					break;
				case 9:
					shiftButton = true;
					break;
				case 10: // reverse
					server.send(new HelmToggleReversePacket());
					shipReverse = !shipReverse;
					break;
			}
		}
		else if (e.type == 1 && e.value == 0)
		{
			if (e.number == 9)
				shiftButton = false;
		}
	}

	@Listener
	public void onConnectSuccess(ConnectionSuccessEvent event)
	{
		server.send(new SetConsolePacket(Console.HELM, true));
		server.send(new ReadyPacket());
	}

	@Listener
	public void onDisconnect(DisconnectEvent event)
	{
		System.out.println("Disconnected: " + event.getCause());
	}
}
