package com.github.thorbenkuck.netcom2.logging;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

/**
 * This Class defines how NetCom2 logging looks.
 * <p>
 * It in and of itself is an implementation from {@link Logging}. So, the other Implementations may simply delegate any
 * call to an instance of this class. Thereby, SRP is maintainable
 *
 * @version 1.0
 * @since 1.0
 */
public class SystemDefaultStyleLogging implements Logging {

	private final PrintStream out;

	public SystemDefaultStyleLogging() {
		this(System.out);
	}

	public SystemDefaultStyleLogging(final PrintStream printStream) {
		out = printStream;
	}

	/**
	 * This Method prints the given String s synchronized to the provided {@link PrintStream} <code>out</code>
	 *
	 * @param s the String, that should be printed
	 */
	private void println(final Object s) {
		out.println(s);
	}

	private String convert(String level) {
		return getPrefix() + level;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "{Default Logging-style for NetCom2Logging}";
	}

	@Override
	public boolean traceEnabled() {
		return true;
	}

	@Override
	public boolean debugEnabled() {
		return true;
	}

	@Override
	public boolean infoEnabled() {
		return true;
	}

	@Override
	public boolean warnEnabled() {
		return true;
	}

	@Override
	public boolean errorEnabled() {
		return true;
	}

	@Override
	public boolean fatalEnabled() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void trace(final Object o) {
		println(convert("TRACE : ") + o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void debug(final Object o) {
		println(convert("DEBUG : ") + o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void info(final Object o) {
		println(convert("INFO : ") + o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void warn(final Object o) {
		println(convert("WARN : ") + o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(final Object o) {
		println(convert("ERROR : ") + o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void catching(final Throwable throwable) {
		StringWriter sw = new StringWriter();
		throwable.printStackTrace(new PrintWriter(sw));
		String stacktrace = sw.toString();
		println(stacktrace);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fatal(final Object o) {
		println(convert("FATAL") + o);
	}

	/**
	 * Returns a unified prefix.
	 * <p>
	 * This is the default prefix, which is universally used by this class.
	 * You may reuse it or ignore it out right.
	 * <p>
	 * It utilizes the {@link LocalDateTime} and {@link Thread#currentThread()} to give you the best information
	 *
	 * @return the prefix
	 */
	String getPrefix() {
		return "[" + LocalDateTime.now() + "] (" + Thread.currentThread().toString() + ") : ";
	}
}
