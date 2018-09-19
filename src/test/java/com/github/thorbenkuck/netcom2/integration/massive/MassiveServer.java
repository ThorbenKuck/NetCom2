package com.github.thorbenkuck.netcom2.integration.massive;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.integration.TestObject;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.Session;

public class MassiveServer {

	public static void main(String[] args) {
		NetComLogging.setLogging(Logging.trace());
		ServerStart serverStart = ServerStart.at(7777);

		serverStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addFirst(System.out::println);

		serverStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addFirst(Session::send);

		try {
			serverStart.launch();
			serverStart.acceptAllNextClients();
		} catch (StartFailedException | ClientConnectionFailedException e) {
			e.printStackTrace();
		}
	}

}
