package com.github.thorbenkuck.netcom2.logging;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.utility.Requirements;

public class NetComLogging implements Logging {

	private static Logging logging = Logging.getDefault();

	@Override
	public void trace(final String s) {
		NetComLogging.getLogging().trace(s);
	}

	@Override
	public void debug(final String s) {
		NetComLogging.getLogging().debug(s);
	}

	@Override
	public void info(final String s) {
		NetComLogging.getLogging().info(s);
	}

	@Override
	public void warn(final String s) {
		NetComLogging.getLogging().warn(s);
	}

	@Override
	public void error(final String s) {
		NetComLogging.getLogging().error(s);
	}

	@Override
	public void error(final String s, final Throwable throwable) {
		NetComLogging.getLogging().error(s, throwable);
	}

	@Override
	public void fatal(final String s) {
		NetComLogging.getLogging().fatal(s);
	}

	@Override
	public void fatal(final String s, final Throwable throwable) {
		NetComLogging.getLogging().fatal(s, throwable);
	}

	@Override
	public void catching(final Throwable throwable) {
		NetComLogging.getLogging().catching(throwable);
	}

	private static Logging getLogging() {
		return logging;
	}

	public static void setLogging(final Logging logging) {
		Requirements.parameterNotNull(logging);
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
