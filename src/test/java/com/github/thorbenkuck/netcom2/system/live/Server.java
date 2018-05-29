package com.github.thorbenkuck.netcom2.system.live;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;

public class Server {

	private ServerStart serverStart;

	public Server() {
		serverStart = ServerStart.at(4444);
	}

	public static void main(String[] args) throws StartFailedException {
		NetComLogging.setLogging(Logging.disabled());
		new Server().start();
	}

	private void register(ServerStart serverStart) {

		serverStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addFirst((connection, session, testObject) -> System.out.println("Received " + testObject.getString() + " from Connection " + connection.getKey()));

	}

	public void start() throws StartFailedException {
		serverStart.launch();

		register(serverStart);

		try {
			serverStart.acceptAllNextClients();
		} catch (ClientConnectionFailedException e) {
			e.printStackTrace();
		}
	}

}
