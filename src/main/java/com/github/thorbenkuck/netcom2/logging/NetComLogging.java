package com.github.thorbenkuck.netcom2.logging;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

public class NetComLogging implements Logging {

	private static Logging logging = Logging.getDefault();

	@Override
	public void trace(String s) {
		NetComLogging.getLogging().trace(s);
	}

	@Override
	public void debug(String s) {
		NetComLogging.getLogging().debug(s);
	}

	@Override
	public void info(String s) {
		NetComLogging.getLogging().info(s);
	}

	@Override
	public void warn(String s) {
		NetComLogging.getLogging().warn(s);
	}

	@Override
	public void error(String s) {
		NetComLogging.getLogging().error(s);
	}

	@Override
	public void error(String s, Throwable throwable) {
		NetComLogging.getLogging().error(s, throwable);
	}

	@Override
	public void fatal(String s) {
		NetComLogging.getLogging().fatal(s);
	}

	@Override
	public void fatal(String s, Throwable throwable) {
		NetComLogging.getLogging().fatal(s, throwable);
	}

	@Override
	public void catching(Throwable throwable) {
		NetComLogging.getLogging().catching(throwable);
	}

	private static Logging getLogging() {
		return logging;
	}

	public static void setLogging(Logging logging) {
		if (logging == null) {
			throw new IllegalArgumentException("Setting Logging to null is prohibited!\n" +
					"Expected an implementation of " + Logging.class + " received: " + null);
		}
		if (NetComLogging.logging == logging) {
			throw new IllegalArgumentException("Cyclic dependency!");
		}
		NetComLogging.logging = logging;
	}

	@Override
	public String toString() {
		return "{Centralized Logging-Mechanism for NetCom2}";
	}
}