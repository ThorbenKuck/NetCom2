package com.github.thorbenkuck.netcom2.logging;

import com.github.thorbenkuck.netcom2.annotations.Synchronized;

/**
 * This Logging implementation ignores all calls and logs nothing.
 *
 * @version 1.0
 * @since 1.0
 */
@Synchronized
public class DisabledLogging implements Logging {

	public DisabledLogging() {
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
	 * <b>This method call will be ignored!</b>
	 * {@inheritDoc}
	 */
	@Override
	public void error(final Object s) {
	}

	/**
	 * <b>This method call will be ignored!</b>
	 * {@inheritDoc}
	 */
	@Override
	public void fatal(final Object s) {
	}

	/**
	 * <b>This method call will be ignored!</b>
	 * {@inheritDoc}
	 */
	@Override
	public void catching(final Throwable throwable) {
	}
}
