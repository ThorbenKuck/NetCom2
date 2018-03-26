package com.github.thorbenkuck.netcom2.logging;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

/**
 * This logging implementation ignores all calls up to warn
 *
 * @version 1.0
 * @since 1.0
 */
public class WarnLogging implements Logging {

	private final Logging style;

	public WarnLogging() {
		this(new SystemDefaultStyleLogging());
	}

	public WarnLogging(final Logging base) {
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
	 * <b>This method call will be ignored!</b>
	 * {@inheritDoc}
	 */
	@Override
	public void info(final String s) {

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
