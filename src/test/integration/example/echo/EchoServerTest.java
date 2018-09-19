package com.github.thorbenkuck.netcom2.integration.example.echo;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.integration.TestObject;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;

public class EchoServerTest {

	private static ServerStart serverStart;

	public static void main(String[] args) {
		NetComLogging.setLogging(Logging.debug());
		// Create the Server at Port 8888
		serverStart = ServerStart.at(8888);

		// And tell the Server, to always send back the Command, he received
		register(serverStart.getCommunicationRegistration());
		try {
			// Than launch the Server, connecting him to the specified port
			serverStart.launch();
			//And accept the connecting clients
			serverStart.acceptAllNextClients();
		} catch (ClientConnectionFailedException | StartFailedException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void register(CommunicationRegistration communicationRegistration) {
		// Alternative:
		// This will echo anything to all Clients.
		// communicationRegistration.addDefaultCommunicationHandler(o -> serverStart.distribute().toAll(o));

		communicationRegistration.register(TestObject.class)
				.addFirst(Session::send);
		communicationRegistration.register(TestObject.class)
				.addFirst((session, o) -> System.out.println("sending back: " + o.getHello()));
	}
}