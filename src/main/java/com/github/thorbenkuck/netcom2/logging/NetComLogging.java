package com.github.thorbenkuck.netcom2.logging;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

public class NetComLogging implements Logging {

	private static Logging logging = Logging.getDefault();

	private static Logging getLogging() {
		return logging;
	}

	public static void setLogging(final Logging logging) {
		NetCom2Utils.parameterNotNull(logging);
		if (NetComLogging.logging == logging) {
			throw new IllegalArgumentException("Cyclic dependency!");
		}
		NetComLogging.logging = logging;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void trace(final String s) {
		NetComLogging.getLogging().trace(s);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void debug(final String s) {
		NetComLogging.getLogging().debug(s);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void info(final String s) {
		NetComLogging.getLogging().info(s);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void warn(final String s) {
		NetComLogging.getLogging().warn(s);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(final String s) {
		NetComLogging.getLogging().error(s);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(final String s, final Throwable throwable) {
		NetComLogging.getLogging().error(s, throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fatal(final String s) {
		NetComLogging.getLogging().fatal(s);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fatal(final String s, final Throwable throwable) {
		NetComLogging.getLogging().fatal(s, throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void catching(final Throwable throwable) {
		NetComLogging.getLogging().catching(throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "{Centralized Logging-Mechanism for NetCom2}";
	}
}
