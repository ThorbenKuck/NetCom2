package com.github.thorbenkuck.netcom2.logging;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.utility.Requirements;

/**
 * This logging implementation uses reflection to determine the caller of any method.
 *
 * For that, this implementation uses the {@link CallerTraceSystemDefaultStyleLogging}.
 */
public class CallerReflectionLogging implements Logging {

	private final Logging style;

	public CallerReflectionLogging() {
		this(new CallerTraceSystemDefaultStyleLogging());
	}

	public CallerReflectionLogging(final Logging base) {
		Requirements.assertNotNull(base);
		this.style = base;
		warn("This Logging-Mechanism is very workload-intensive!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void trace(final String s) {
		style.trace(s);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void debug(final String s) {
		style.debug(s);
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
		error(s);
		catching(throwable);
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
		fatal(s);
		catching(throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void catching(final Throwable throwable) {
		style.catching(throwable);
	}
}
