package com.github.thorbenkuck.netcom2.integration.nio;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.integration.TestObject;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.modules.ModuleFactory;

public class NIOServer {

	private final ServerStart serverStart;

	public NIOServer(int port) {
		this.serverStart = ServerStart.at(port);
	}

	public static void main(String[] args) {
		NetComLogging.setLogging(Logging.trace());
		NIOServer server = new NIOServer(4444);
		server.run();
	}

	public void run() {
		ModuleFactory.access()
				.nio()
				.setBufferSize(1024)
				.apply(serverStart);

		serverStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addFirst(this::handle);

		try {
			serverStart.launch();
			serverStart.acceptAllNextClients();
		} catch (StartFailedException | ClientConnectionFailedException e) {
			e.printStackTrace();
		}
	}

	private void handle(Session session, TestObject testObject) {
		System.out.println(testObject.getHello());
		session.send(testObject);
	}
}
