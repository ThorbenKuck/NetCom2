package com.github.thorbenkuck.netcom2.logging;

import com.github.thorbenkuck.netcom2.annotations.Synchronized;

/**
 * This implementation of {@link Logging} ignores the trace method
 *
 * @version 1.0
 * @since 1.0
 */
@Synchronized
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
	public void trace(final Object s) {
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
