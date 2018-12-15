package com.github.thorbenkuck.netcom2.logging;

/**
 * This is an instance of Logging, that logs every step.
 *
 * @version 1.0
 * @since 1.0
 */
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
	public void trace(final Object o) {
		style.trace(o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void debug(final Object o) {
		style.debug(o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void info(final Object o) {
		style.info(o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void warn(final Object o) {
		style.warn(o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(final Object o) {
		style.error(o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fatal(final Object o) {
		style.fatal(o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void catching(final Throwable throwable) {
		style.catching(throwable);
	}
}
