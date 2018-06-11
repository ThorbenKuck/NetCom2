package com.github.thorbenkuck.netcom2.integration.tcp;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import org.junit.Ignore;

@Ignore
public class TCPServerTest {

	public TCPServerTest() {
		final ServerStart serverStart = ServerStart.at(4545);
		register(serverStart.getCommunicationRegistration());
		try {
			serverStart.launch();

			serverStart.acceptAllNextClients();
		} catch (StartFailedException | ClientConnectionFailedException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		NetComLogging.setLogging(Logging.trace());
		new TCPServerTest();
	}

	private void register(CommunicationRegistration communicationRegistration) {
		communicationRegistration.addDefaultCommunicationHandler(((connection, session, o) -> session.send(o)));
	}

}
