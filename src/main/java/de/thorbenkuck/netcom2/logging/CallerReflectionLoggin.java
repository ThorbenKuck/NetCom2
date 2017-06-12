package de.thorbenkuck.netcom2.logging;

import de.thorbenkuck.netcom2.network.interfaces.Logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

public class CallerReflectionLoggin implements Logging {

	public CallerReflectionLoggin() {
		warn("This Logging-Mechanism is very workload-intensive!");
	}

	public String getPrefix() {
		return ("[" + new Date().toString() + "] (" + Thread.currentThread().toString() + ") [" + getCaller() + "] ");
	}

	public String getCaller() {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		for (StackTraceElement stackTraceElement : stackTraceElements) {
			if (! stackTraceElement.getClassName().equals(CallerReflectionLoggin.class.getName())
					&& ! stackTraceElement.getClassName().equals(NetComLogging.class.getName())
					&& stackTraceElement.getClassName().indexOf("java.lang.Thread") != 0) {
				return stackTraceElement.getClassName();
			}
		}
		return null;
	}

	@Override
	public void trace(String s) {
		System.out.println(getPrefix() + " TRACE : " + s);
	}

	@Override
	public void debug(String s) {
		System.out.println(getPrefix() + " DEBUG : " + s);
	}

	@Override
	public void info(String s) {
		System.out.println(getPrefix() + " INFO : " + s);
	}

	@Override
	public void warn(String s) {
		System.out.println(getPrefix() + " WARN : " + s);
	}

	@Override
	public void error(String s) {
		System.out.println(getPrefix() + " ERROR : " + s);
	}

	@Override
	public void error(String s, Throwable throwable) {
		error(s);
		catching(throwable);
	}

	@Override
	public void fatal(String s) {
		System.out.println(getPrefix() + " FATAL : " + s);
	}

	@Override
	public void fatal(String s, Throwable throwable) {
		fatal(s);
		catching(throwable);
	}

	@Override
	public void catching(Throwable throwable) {
		StringWriter sw = new StringWriter();
		throwable.printStackTrace(new PrintWriter(sw));
		String stacktrace = sw.toString();
		System.out.println(stacktrace);
	}

}
