package com.github.thorbenkuck.netcom2;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;
import com.github.thorbenkuck.netcom2.utility.threaded.NetComThreadPool;

public class ServerStartTest {

	public static void main(String[] args) throws Exception {
		NetComLogging.setLogging(Logging.trace());
		NetComThreadPool.startWorkerTask();
		NetComThreadPool.startWorkerTask();
		NetComThreadPool.startWorkerTask();
		NetComThreadPool.startWorkerTask();
		try {
			new ServerStartTest().run();
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	private void print(TestObject testObject) {
		System.out.println(Thread.currentThread() + ": " + testObject);
	}

	private void run() throws StartFailedException, ClientConnectionFailedException {
		serverStart.addClientConnectedHandler(client -> System.out.println("Found connected client!"));
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

	private final ServerStart serverStart;

	ServerStartTest() {
		serverStart = ServerStart.at(4444);
	}
}
