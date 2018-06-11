package com.github.thorbenkuck.netcom2.integration;

import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

public class LoggingExample {

	private String address = "";
	private int port = 0;

	public static void main(String[] args) {
		new LoggingExample().run();
	}

	public void run() {
		trace();
		callerTrace();
		debug();
		info();
		warn();
		error();
	}

	public void trace() {
		Logging logging = Logging.trace();
		System.out.println("TRACE");
		System.out.println();
		NetComLogging.setLogging(Logging.disabled());
		ClientStart clientStart = ClientStart.at(address, port);
		NetComLogging.setLogging(logging);
		try {
			clientStart.launch();
		} catch (StartFailedException ignored) {
		}
		NetComLogging.setLogging(Logging.disabled());
		System.out.println();
		System.out.println();
	}

	public void callerTrace() {
		Logging logging = Logging.callerTrace();
		System.out.println("CALLER-TRACE");
		System.out.println();
		NetComLogging.setLogging(Logging.disabled());
		ClientStart clientStart = ClientStart.at(address, port);
		NetComLogging.setLogging(logging);
		try {
			clientStart.launch();
		} catch (StartFailedException ignored) {
			logging.catching(ignored);
		}
		NetComLogging.setLogging(Logging.disabled());
		System.out.println();
		System.out.println();
	}

	public void debug() {
		Logging logging = Logging.debug();
		System.out.println("DEBUG");
		System.out.println();
		NetComLogging.setLogging(Logging.disabled());
		ClientStart clientStart = ClientStart.at(address, port);
		NetComLogging.setLogging(logging);
		try {
			clientStart.launch();
		} catch (StartFailedException ignored) {
			logging.catching(ignored);
		}
		NetComLogging.setLogging(Logging.disabled());
		System.out.println();
		System.out.println();
	}

	public void info() {
		Logging logging = Logging.info();
		System.out.println("INFO");
		System.out.println();
		NetComLogging.setLogging(Logging.disabled());
		ClientStart clientStart = ClientStart.at(address, port);
		NetComLogging.setLogging(logging);
		try {
			clientStart.launch();
		} catch (StartFailedException ignored) {
			logging.catching(ignored);
		}
		NetComLogging.setLogging(Logging.disabled());
		System.out.println();
		System.out.println();
	}

	public void warn() {
		Logging logging = Logging.warn();
		System.out.println("WARN");
		System.out.println();
		NetComLogging.setLogging(Logging.disabled());
		ClientStart clientStart = ClientStart.at(address, port);
		NetComLogging.setLogging(logging);
		try {
			clientStart.launch();
		} catch (StartFailedException ignored) {
			logging.catching(ignored);
		}
		NetComLogging.setLogging(Logging.disabled());
		System.out.println();
		System.out.println();
	}

	public void error() {
		Logging logging = Logging.error();
		System.out.println("ERROR");
		System.out.println();
		NetComLogging.setLogging(Logging.disabled());
		ClientStart clientStart = ClientStart.at(address, port);
		NetComLogging.setLogging(Logging.error());
		try {
			clientStart.launch();
		} catch (StartFailedException ignored) {
			logging.catching(ignored);
		}
		NetComLogging.setLogging(Logging.disabled());
		System.out.println();
		System.out.println();
	}

}
