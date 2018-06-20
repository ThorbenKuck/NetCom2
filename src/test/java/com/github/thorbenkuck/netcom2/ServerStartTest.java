package com.github.thorbenkuck.netcom2;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;

public class ServerStartTest {

	public static void main(String[] args) throws Exception {
		NetComLogging.setLogging(Logging.warn());
		try {
			new ServerStartTest().run();
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	private void run() throws StartFailedException, ClientConnectionFailedException {
		serverStart.addClientConnectedHandler(client -> System.out.println("Found connected client!"));
		serverStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addFirst(Session::send);
		serverStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addFirst(System.out::println);
		serverStart.launch();
		serverStart.acceptAllNextClients();
	}

	private final ServerStart serverStart;

	ServerStartTest() {
		serverStart = ServerStart.at(4444);
	}
}
