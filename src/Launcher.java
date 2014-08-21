import java.io.IOException;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import static java.util.Arrays.asList;

public class Launcher
{
	public static void main(String[] args) throws IOException
	{
		OptionParser parser = new OptionParser();
		OptionSpec<Integer> port = parser.acceptsAll(asList("p", "port"), "port to connect to").withRequiredArg().describedAs("port").ofType(Integer.class).defaultsTo(2010);
		OptionSpec<String> host = parser.acceptsAll(asList("h", "host", "hostname"), "hostname of server").withRequiredArg().ofType(String.class).describedAs("hostname").required();
		OptionSpec<Integer> proxy = parser.acceptsAll(asList("P", "proxy"), "run as proxy listening on specified port").withOptionalArg().ofType(Integer.class).describedAs("port").defaultsTo(2010);
		OptionSpec<Integer> debug = parser.acceptsAll(asList("D", "debug"), "run as debug proxy printing parsed packets").withOptionalArg().ofType(Integer.class).describedAs("port").defaultsTo(2010);
		OptionSpec<Integer> rawdebug = parser.acceptsAll(asList("R", "rawdebug"), "run as debug proxy printing unparsed packets").withOptionalArg().ofType(Integer.class).describedAs("port").defaultsTo(2010);
		parser.acceptsAll(asList("c", "client"), "print packets from client");
		parser.acceptsAll(asList("s", "server"), "print packets from server");
		parser.acceptsAll(asList("?", "help"), "show help").forHelp();

		try
		{
			OptionSet options = parser.parse(args);
			if (options.has(proxy))
				new Thread(new HelmProxy(options.valueOf(host), options.valueOf(port), options.valueOf(proxy))).start();
			else if (options.has(debug))
				new Thread(new DebugProxy(options.valueOf(host), options.valueOf(port), options.valueOf(debug), true, true, options.has("client"), options.has("server"))).start();
			else if (options.has(rawdebug))
				new Thread(new DebugProxy(options.valueOf(host), options.valueOf(port), options.valueOf(rawdebug), false, false, options.has("client"), options.has("server"))).start();
			else
				new HelmController(options.valueOf(host), options.valueOf(port));
		}
		catch (OptionException e)
		{
			parser.printHelpOn(System.out);
			return;
		}
		catch (IllegalArgumentException e)
		{
			parser.printHelpOn(System.out);
			return;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return;
		}
	}

	private static void printUsage()
	{
		System.out.println("Usage: artenhance [OPTIONS] -s <hostname>");
		System.out.println("Options:");
		System.out.println("\t-h\tShow this summary of options.");
		System.out.println("\t-s hostname");
		System.out.println("\t\tHost server to connect to. Required.");
		System.out.println("\t-p port");
		System.out.println("\t\tPort on server to connect to; defaults to\n\t\t2010 (artemis default) if omitted.");
		System.out.println("\t-P [port]");
		System.out.println("\t\tSet up proxy server to connect, listening on given port\n\t\tor 2010 if none specified.");
	}
}
