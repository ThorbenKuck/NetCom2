package test.examples.echo;

import de.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import de.thorbenkuck.netcom2.exceptions.StartFailedException;
import de.thorbenkuck.netcom2.network.server.ServerStart;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import test.examples.TestObject;

public class EchoServerTest {

	private static ServerStart serverStart;

	public static void main(String[] args) {
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
				.addFirst((user, o) -> System.out.println("sending back: " + o));
	}
}