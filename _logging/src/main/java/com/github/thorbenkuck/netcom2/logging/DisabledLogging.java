package com.github.thorbenkuck.netcom2.logging;

/**
 * This Logging implementation ignores all calls and logs nothing.
 *
 * @version 1.0
 * @since 1.0
 */
public class DisabledLogging implements Logging {

	public DisabledLogging() {
		instantiated(this);
	}

	@Override
	public boolean traceEnabled() {
		return false;
	}

	@Override
	public boolean debugEnabled() {
		return false;
	}

	@Override
	public boolean infoEnabled() {
		return false;
	}

	@Override
	public boolean warnEnabled() {
		return false;
	}

	@Override
	public boolean errorEnabled() {
		return false;
	}

	@Override
	public boolean fatalEnabled() {
		return false;
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
	 * <b>This method call will be ignored!</b>
	 * {@inheritDoc}
	 */
	@Override
	public void error(final Object o) {
	}

	/**
	 * <b>This method call will be ignored!</b>
	 * {@inheritDoc}
	 */
	@Override
	public void fatal(final Object o) {
	}

	/**
	 * <b>This method call will be ignored!</b>
	 * {@inheritDoc}
	 */
	@Override
	public void catching(final Throwable throwable) {
	}
}
