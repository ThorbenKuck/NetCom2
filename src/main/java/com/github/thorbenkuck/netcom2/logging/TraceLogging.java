package com.github.thorbenkuck.netcom2.logging;

import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

/**
 * This is an instance of Logging, that logs every step.
 *
 * @version 1.0
 * @since 1.0
 */
@Synchronized
public class TraceLogging implements Logging {

	private final Logging style;

	public TraceLogging() {
		this(new SystemDefaultStyleLogging());
	}

	public TraceLogging(final Logging base) {
		this.style = base;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void trace(final String s) {
		style.trace(s);
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
