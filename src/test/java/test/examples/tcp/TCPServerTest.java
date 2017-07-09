package test.examples.tcp;

import de.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import de.thorbenkuck.netcom2.exceptions.StartFailedException;
import de.thorbenkuck.netcom2.logging.NetComLogging;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.server.ServerStart;
import de.thorbenkuck.netcom2.network.shared.clients.ConnectionFactory;
import de.thorbenkuck.netcom2.network.shared.clients.ConnectionFactoryHook;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;

public class TCPServerTest {

	public static void main(String[] args) {
		ConnectionFactory.setConnectionFactoryHook(ConnectionFactoryHook.tcp());
		NetComLogging.setLogging(Logging.trace());
		new TCPServerTest();
	}

	private ServerStart serverStart;

	public TCPServerTest() {
		serverStart = ServerStart.at(4545);
		register(serverStart.getCommunicationRegistration());
		try {
			serverStart.launch();

			serverStart.acceptAllNextClients();
		} catch (StartFailedException | ClientConnectionFailedException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void register(CommunicationRegistration communicationRegistration) {
		communicationRegistration.addDefaultCommunicationHandler(((connection, session, o) -> session.send(o)));
	}

}
