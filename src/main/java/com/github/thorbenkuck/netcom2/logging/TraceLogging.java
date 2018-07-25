package com.github.thorbenkuck.netcom2.logging;

import com.github.thorbenkuck.netcom2.annotations.Synchronized;

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
		instantiated(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void trace(final Object s) {
		style.trace(s);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void debug(final Object s) {
		style.debug(s);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void info(final Object s) {
		style.info(s);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void warn(final Object s) {
		style.warn(s);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(final Object s) {
		style.error(s);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fatal(final Object s) {
		style.fatal(s);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void catching(final Throwable throwable) {
		style.catching(throwable);
	}
}
