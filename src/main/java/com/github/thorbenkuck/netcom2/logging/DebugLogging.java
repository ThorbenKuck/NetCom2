package com.github.thorbenkuck.netcom2.logging;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

/**
 * This implementation of {@link Logging} ignores the trace method
 */
public class DebugLogging implements Logging {

	private final Logging style;

	public DebugLogging() {
		this(new SystemDefaultStyleLogging());
	}

	public DebugLogging(final Logging base) {
		this.style = base;
	}

	/**
	 * <b>The call of this Method is ignored!</b>
	 * {@inheritDoc}
	 */
	@Override
	public void trace(final String s) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void debug(final String s) {
		style.debug(s);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void info(final String s) {
		style.info(s);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void warn(final String s) {
		style.warn(s);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(final String s) {
		style.error(s);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(final String s, final Throwable throwable) {
		style.error(s, throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fatal(final String s) {
		style.fatal(s);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fatal(final String s, final Throwable throwable) {
		style.fatal(s, throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void catching(final Throwable throwable) {
		style.catching(throwable);
	}
}
