package com.github.thorbenkuck.netcom2.logging;

import com.github.thorbenkuck.netcom2.annotations.Synchronized;

/**
 * This {@link Logging} implementation ignores all calls up to info
 *
 * @version 1.0
 * @since 1.0
 */
@Synchronized
public class InfoLogging implements Logging {

	private final Logging style;

	public InfoLogging() {
		this(new SystemDefaultStyleLogging());
	}

	public InfoLogging(final Logging base) {
		this.style = base;
	}

	/**
	 * <b>This method call will be ignored!</b>
	 * {@inheritDoc}
	 */
	@Override
	public void trace(final String s) {
	}

	/**
	 * <b>This method call will be ignored!</b>
	 * {@inheritDoc}
	 */
	@Override
	public void debug(final String s) {
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
