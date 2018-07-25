package com.github.thorbenkuck.netcom2.logging;

import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

/**
 * This logging implementation uses reflection to determine the caller of any method.
 * <p>
 * For that, this implementation uses the {@link CallerTraceSystemDefaultStyleLogging}.
 * <p>
 * Since this class is using the {@link CallerTraceSystemDefaultStyleLogging}, using this logging version is extremely
 * workload intensive. Trace is the most common call and in nearly all methods, at least one of this call can be found.
 * This means, that with this Logging implementation, in nearly all methods the stacktrace will be analysed at least once.
 *
 * @version 1.0
 * @since 1.0
 */
@Synchronized
public class CallerReflectionLogging implements Logging {

	private final Logging style;

	public CallerReflectionLogging() {
		this(new CallerTraceSystemDefaultStyleLogging());
	}

	public CallerReflectionLogging(final Logging base) {
		NetCom2Utils.assertNotNull(base);
		this.style = base;
		warn("This Logging-Mechanism is very workload-intensive!");
		instantiated(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void trace(final Object s) {
		style.trace(s);
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
