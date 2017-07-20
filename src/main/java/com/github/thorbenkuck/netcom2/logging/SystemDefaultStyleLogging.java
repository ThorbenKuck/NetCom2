package com.github.thorbenkuck.netcom2.logging;

import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Synchronized
public class SystemDefaultStyleLogging implements Logging {

	private final PrintStream out;
	private final Lock printLock = new ReentrantLock();

	public SystemDefaultStyleLogging() {
		this(System.out);
	}

	public SystemDefaultStyleLogging(PrintStream printStream) {
		out = printStream;
	}

	@Override
	public String toString() {
		return "{Default Logging-style for NetCom2Logging}";
	}

	@Override
	public void trace(String s) {
		try {
			printLock.lock();
			println(getPrefix() + "TRACE : " + s);
		} finally {
			printLock.unlock();
		}
	}

	@Override
	public void debug(String s) {
		try {
			printLock.lock();
			println(getPrefix() + "DEBUG : " + s);
		} finally {
			printLock.unlock();
		}
	}

	@Override
	public void info(String s) {
		try {
			printLock.lock();
			println(getPrefix() + "INFO : " + s);
		} finally {
			printLock.unlock();
		}
	}

	@Override
	public void warn(String s) {
		try {
			printLock.lock();
			println(getPrefix() + "WARN : " + s);
		} finally {
			printLock.unlock();
		}
	}

	@Override
	public void error(String s) {
		boolean locked = false;
		try {
			locked = printLock.tryLock();
			println(getPrefix() + "ERROR : " + s);
		} finally {
			if (locked) printLock.unlock();
		}
	}

	@Override
	public void error(String s, Throwable throwable) {
		try {
			printLock.lock();
			error(s);
			catching(throwable);
		} finally {
			printLock.unlock();
		}
	}

	@Override
	public void fatal(String s) {
		boolean locked = false;
		try {
			locked = printLock.tryLock();
			println(getPrefix() + "FATAL : " + s);
		} finally {
			if (locked) printLock.unlock();
		}
	}

	@Override
	public void fatal(String s, Throwable throwable) {
		try {
			printLock.lock();
			fatal(s);
			catching(throwable);
		} finally {
			printLock.unlock();
		}
	}

	@Override
	public void catching(Throwable throwable) {
		boolean locked = false;
		try {
			locked = printLock.tryLock();
			StringWriter sw = new StringWriter();
			throwable.printStackTrace(new PrintWriter(sw));
			String stacktrace = sw.toString();
			println(stacktrace);
		} finally {
			if (locked) printLock.unlock();
		}
	}

	private void println(String s) {
		synchronized (out) {
			out.println(s);
		}
	}

	String getPrefix() {
		return "[" + LocalDateTime.now() + "] (" + Thread.currentThread().toString() + ") ";
	}


}
