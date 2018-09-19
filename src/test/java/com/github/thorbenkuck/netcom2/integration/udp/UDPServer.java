package com.github.thorbenkuck.netcom2.integration.udp;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.integration.TestObject;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.Session;

public class UDPServer {

	public static void main(String[] args) throws StartFailedException, ClientConnectionFailedException {
		ServerStart serverStart = ServerStart.udp(4444);

		serverStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addFirst(Session::send);

		serverStart.launch();
		serverStart.acceptAllNextClients();
	}
}
