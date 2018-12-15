package com.github.thorbenkuck.netcom2.logging;

/**
 * This implementation of {@link Logging} ignores the trace method
 *
 * @version 1.0
 * @since 1.0
 */
public class DebugLogging implements Logging {

	private final Logging style;

	public DebugLogging() {
		this(new SystemDefaultStyleLogging());
	}

	public DebugLogging(final Logging base) {
		this.style = base;
		instantiated(this);
	}

	/**
	 * <b>The call of this Method is ignored!</b>
	 * {@inheritDoc}
	 */
	@Override
	public void trace(final Object o) {
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
