package com.github.thorbenkuck.netcom2.logging;

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
public class CallerReflectionLogging implements Logging {

	private final Logging style;

	public CallerReflectionLogging() {
		this(new CallerTraceSystemDefaultStyleLogging());
	}

	public CallerReflectionLogging(final Logging base) {
		this.style = base;
		warn("This Logging-Mechanism is very workload-intensive!");
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
	 * {@inheritDoc}
	 */
	@Override
	public void trace(final Object o) {
		style.trace(o);
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
