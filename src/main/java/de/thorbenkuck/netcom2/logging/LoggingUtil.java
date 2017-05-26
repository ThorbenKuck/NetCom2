package de.thorbenkuck.netcom2.logging;

import de.thorbenkuck.netcom2.network.interfaces.Logging;

public class LoggingUtil implements Logging {

	private static Logging logging = new SystemLogging();

	@Override
	public void catching(Throwable throwable) {
		getLogging().catching(throwable);
	}

	public static Logging getLogging() {
		return logging;
	}

	public static void setLogging(Logging logging) {
		if (logging == null) {
			throw new IllegalArgumentException("Setting Logging to null is prohibited!\n" +
					"Expected an implementation at " + Logging.class + " received: " + null);
		}
		LoggingUtil.logging = logging;
	}

	@Override
	public void debug(String s) {
		getLogging().debug(s);
	}

	@Override
	public void info(String s) {
		getLogging().info(s);
	}

	@Override
	public void trace(String s) {
		getLogging().trace(s);
	}

	@Override
	public void warn(String s) {
		getLogging().warn(s);
	}

	@Override
	public void error(String s) {
		getLogging().error(s);
	}

	@Override
	public void error(String s, Throwable throwable) {
		getLogging().error(s, throwable);
	}

	@Override
	public void fatal(String s) {
		getLogging().fatal(s);
	}

	@Override
	public void fatal(String s, Throwable throwable) {
		getLogging().fatal(s, throwable);
	}

	@Override
	public String toString() {
		return "{Central Logging-Mechanism for NetCom2}";
	}
}
