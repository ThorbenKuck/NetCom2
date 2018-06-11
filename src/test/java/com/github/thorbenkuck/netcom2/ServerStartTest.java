package com.github.thorbenkuck.netcom2;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;

public class ServerStartTest {

	public static void main(String[] args) throws Exception {
		NetComLogging.setLogging(Logging.trace());
		try {
			new ServerStartTest().run();
		} catch (Exception e) {
			e.printStackTrace(System.out);
			throw e;
		}
	}

	private void run() throws StartFailedException, ClientConnectionFailedException {
		serverStart.addClientConnectedHandler(client -> System.out.println("Found connected client!"));
		serverStart.launch();
		serverStart.acceptAllNextClients();
	}

	private final ServerStart serverStart;

	ServerStartTest() {
		serverStart = ServerStart.at(4444);
	}
}
