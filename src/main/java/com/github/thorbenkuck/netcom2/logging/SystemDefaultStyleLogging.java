package com.github.thorbenkuck.netcom2.logging;

import com.github.thorbenkuck.netcom2.annotations.Synchronized;

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
@Synchronized
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
	private void println(final String s) {
		synchronized (out) {
			out.println(s);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "{Default Logging-style for NetCom2Logging}";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void trace(final String s) {
		println(getPrefix() + "TRACE : " + s);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void debug(final String s) {
		println(getPrefix() + "DEBUG : " + s);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void info(final String s) {
		println(getPrefix() + "INFO : " + s);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void warn(final String s) {
		println(getPrefix() + "WARN : " + s);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(final String s) {
		println(getPrefix() + "ERROR : " + s);
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
		println(getPrefix() + "FATAL : " + s);
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
		StringWriter sw = new StringWriter();
		throwable.printStackTrace(new PrintWriter(sw));
		String stacktrace = sw.toString();
		println(stacktrace);
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
		return "[" + LocalDateTime.now() + "] (" + Thread.currentThread().toString() + ") ";
	}
}
