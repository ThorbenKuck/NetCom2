package com.github.thorbenkuck.netcom2.test;

import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

public class Test {

	private String address = "localhost";
	private int port = 4444;

	public void run() {

		NetComLogging.setLogging(Logging.trace());
		NetComLogging.setLogging(Logging.debug());
		NetComLogging.setLogging(Logging.info());
		NetComLogging.setLogging(Logging.warn());
		NetComLogging.setLogging(Logging.error());
		NetComLogging.setLogging(Logging.disabled());


		NetComLogging.setLogging(Logging.callerTrace());


	}

}
