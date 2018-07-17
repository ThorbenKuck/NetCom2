package com.github.thorbenkuck.netcom2.massive;

import com.github.thorbenkuck.netcom2.TestObject;
import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;

public class MassiveServer {

	public static void main(String[] args) {
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
