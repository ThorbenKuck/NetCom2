package com.github.thorbenkuck.netcom2.tcp;

import com.github.thorbenkuck.netcom2.TestObject;
import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;

public class TCPServer {

	public static final int TCP_TEST_PORT = 4567;

	public static void main(String[] args) throws StartFailedException, ClientConnectionFailedException {
		NetComLogging.setLogging(Logging.trace());
		ServerStart serverStart = ServerStart.tcp(4567);
		serverStart.addClientConnectedHandler(client -> client.addDisconnectedHandler(c -> System.out.println("Client Disconnected")));
		serverStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addFirst(System.out::println);
		serverStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addFirst(Session::send);
		serverStart.launch();
		serverStart.acceptAllNextClients();
	}

}
