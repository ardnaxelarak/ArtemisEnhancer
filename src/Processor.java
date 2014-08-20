import java.io.IOException;

import net.dhleong.acl.enums.MainScreenView;
import net.dhleong.acl.iface.ArtemisNetworkInterface;
import net.dhleong.acl.iface.ThreadedArtemisNetworkInterface;
import net.dhleong.acl.protocol.core.*;
import net.dhleong.acl.protocol.core.helm.*;
import net.dhleong.acl.protocol.core.weap.*;
import net.dhleong.acl.util.BoolState;
import net.dhleong.acl.vesseldata.VesselData;
import net.dhleong.acl.world.ArtemisPlayer;

public class Processor
{
	protected ArtemisNetworkInterface server;
	private static float RUDDER_DEADZONE = 0.1f;
	private static float IMPULSE_DEADZONE = 0.1f;
	private static float CLIMBDIVE_DEADZONE = 0.1f;

	protected boolean shieldsUp = false;
	private boolean shiftButton = false;
	protected boolean shipReverse = false;
	protected int shipWarp = 0;
	protected float shipImpulse = 0f;
	protected float impulseControl = 0f;
	protected MainScreenView frontScreen = MainScreenView.FORE;

	public Processor(ArtemisNetworkInterface server) throws IOException
	{
		this.server = server;
	}

	public void listen()
	{
		Joystick j = new Joystick("/dev/input/js0", false);
		while (true)
		{
			Event e = j.getEvent();
			if (e != null)
				process(e);
		}
	}

	protected void updateCurrentState(ArtemisPlayer player)
	{
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
					int sgn = 1;
					if (value < 0)
					{
						value *= -1;
						sgn = -1;
					}
					value -= IMPULSE_DEADZONE;
					if (value < 0)
						value = 0;
					value /= 1 - IMPULSE_DEADZONE;
					if (value == 0)
						sgn = 0;
					if (sgn > 0)
					{
						if (value > shipImpulse)
						{
							shipImpulse = value;
							server.send(new HelmSetImpulsePacket(value));
						}
					}
					else if (sgn < 0)
					{
						if (shipImpulse > 1 - value)
						{
							shipImpulse = 1 - value;
							server.send(new HelmSetImpulsePacket(1 - value));
						}
					}
					break;
				case 3: // pitch
					value = e.value / 32767f;
					server.send(new HelmSetClimbDivePacket(value));
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
}
