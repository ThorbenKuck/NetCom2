package com.github.thorbenkuck.netcom2.logging;

import com.github.thorbenkuck.netcom2.annotations.Synchronized;

/**
 * This Logging implementation ignores all calls up to {@link Logging#error()}
 *
 * @version 1.0
 * @since 1.0
 */
@Synchronized
public class ErrorLogging implements Logging {

	private final Logging style;

	public ErrorLogging() {
		this(new SystemDefaultStyleLogging());
	}

	public ErrorLogging(final Logging base) {
		this.style = base;
		instantiated(this);
	}

	/**
	 * <b>This method call will be ignored!</b>
	 * {@inheritDoc}
	 */
	@Override
	public void trace(final Object s) {

	}

	/**
	 * <b>This method call will be ignored!</b>
	 * {@inheritDoc}
	 */
	@Override
	public void debug(final Object s) {

	}

	/**
	 * <b>This method call will be ignored!</b>
	 * {@inheritDoc}
	 */
	@Override
	public void info(final Object s) {

	}

	/**
	 * <b>This method call will be ignored!</b>
	 * {@inheritDoc}
	 */
	@Override
	public void warn(final Object s) {

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
