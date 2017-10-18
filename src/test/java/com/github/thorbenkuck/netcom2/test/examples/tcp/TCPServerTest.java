package com.github.thorbenkuck.netcom2.test.examples.tcp;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.clients.ConnectionFactory;
import com.github.thorbenkuck.netcom2.network.shared.clients.ConnectionFactoryHook;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;

public class TCPServerTest {

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

	public static void main(String[] args) {
		ConnectionFactory.setConnectionFactoryHook(ConnectionFactoryHook.tcp());
		NetComLogging.setLogging(Logging.trace());
		new TCPServerTest();
	}

}
