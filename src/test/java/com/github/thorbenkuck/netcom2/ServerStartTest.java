package com.github.thorbenkuck.netcom2;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.UnhandledExceptionContainer;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;

public class ServerStartTest {

	private static int encountered = 0;
	private final ServerStart serverStart;

	ServerStartTest() {
		serverStart = ServerStart.at(4444);
	}

	public static void main(String[] args) throws Exception {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println("Encountered " + encountered + " Exceptions!")));
		NetComLogging.setLogging(Logging.trace());
		UnhandledExceptionContainer.addHandler(t -> {
			synchronized (ServerStartTest.class) {
				++encountered;
			}
		});
		try {
			new ServerStartTest().run();
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	private void print(TestObject testObject) {
		System.out.println(Thread.currentThread() + ": " + testObject);
	}

	private void connected(Client client) {
		System.out.println("Found connected client!");
		client.addDisconnectedHandler(disconnected -> System.out.println("Client disconnected"));
	}

	private void run() throws StartFailedException, ClientConnectionFailedException {
		serverStart.addClientConnectedHandler(this::connected);
		serverStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addFirst(Session::send)
				.require(Session::isIdentified);
		serverStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addFirst(this::print)
				.require(Session::isIdentified);
		serverStart.getCommunicationRegistration()
				.register(Login.class)
				.addFirst((session, login) -> session.setIdentified(true))
				.require(session -> !session.isIdentified());
		serverStart.getCommunicationRegistration()
				.register(Logout.class)
				.addFirst((session, logout) -> session.setIdentified(false));

		serverStart.launch();
		serverStart.acceptAllNextClients();
	}
}
