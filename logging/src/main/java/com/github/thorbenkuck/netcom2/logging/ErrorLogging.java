package com.github.thorbenkuck.netcom2.logging;

/**
 * This Logging implementation ignores all calls up to {@link Logging#error()}
 *
 * @version 1.0
 * @since 1.0
 */
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
	public void trace(final Object o) {

	}

	/**
	 * <b>This method call will be ignored!</b>
	 * {@inheritDoc}
	 */
	@Override
	public void debug(final Object o) {

	}

	/**
	 * <b>This method call will be ignored!</b>
	 * {@inheritDoc}
	 */
	@Override
	public void info(final Object o) {

	}

	/**
	 * <b>This method call will be ignored!</b>
	 * {@inheritDoc}
	 */
	@Override
	public void warn(final Object o) {

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
