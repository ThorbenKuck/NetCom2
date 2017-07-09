package de.thorbenkuck.netcom2.logging;

import de.thorbenkuck.netcom2.network.interfaces.Logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Objects;

public class CallerReflectionLogging implements Logging {

	private Logging logging;

	public CallerReflectionLogging(Logging base) {
		Objects.requireNonNull(base);
		warn("This Logging-Mechanism is very workload-intensive!");
		this.logging = base;
	}

	public CallerReflectionLogging() {
		this(new SystemDefaultStyleLogging());
	}

	public String getPrefix() {
		return (" [" + getCaller() + "] ");
	}

	public String getCaller() {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		for (StackTraceElement stackTraceElement : stackTraceElements) {
			if (! stackTraceElement.getClassName().equals(CallerReflectionLogging.class.getName())
					&& ! stackTraceElement.getClassName().equals(NetComLogging.class.getName())
					&& stackTraceElement.getClassName().indexOf("java.lang.Thread") != 0) {
				return stackTraceElement.getClassName();
			}
		}
		return null;
	}

	@Override
	public void trace(String s) {
		logging.trace(getPrefix() + " TRACE : " + s);
	}

	@Override
	public void debug(String s) {
		logging.debug(getPrefix() + " DEBUG : " + s);
	}

	@Override
	public void info(String s) {
		logging.info(getPrefix() + " INFO : " + s);
	}

	@Override
	public void warn(String s) {
		logging.warn(getPrefix() + " WARN : " + s);
	}

	@Override
	public void error(String s) {
		logging.error(getPrefix() + " ERROR : " + s);
	}

	@Override
	public void error(String s, Throwable throwable) {
		error(s);
		catching(throwable);
	}

	@Override
	public void fatal(String s) {
		logging.fatal(getPrefix() + " FATAL : " + s);
	}

	@Override
	public void fatal(String s, Throwable throwable) {
		fatal(s);
		catching(throwable);
	}

	@Override
	public void catching(Throwable throwable) {
		logging.catching(throwable);
	}

}
