package com.github.thorbenkuck.netcom2.connections;

import com.github.thorbenkuck.netcom2.TestObject;
import com.github.thorbenkuck.netcom2.exceptions.ConnectionEstablishmentFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.client.Sender;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.connections.ConnectionContext;

public class Client {

	private static int receivedCount = 0;

	public static void main(String[] args) {
		NetComLogging.setLogging(Logging.trace());
		ClientStart clientStart = ClientStart.at("localhost", 4569);

		clientStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addFirst(Client::print);

		try {
			clientStart.launch();
		} catch (StartFailedException e) {
			e.printStackTrace();
			System.exit(1);
		}

		Sender sender = Sender.open(clientStart);

		sender.objectToServer(new TestObject("From DefaultConnection"));

		try {
			clientStart.newConnection(TestConnectionKey.class).synchronize();
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
			sender.objectToServer(new TestObject("From DefaultConnection"));
			sender.objectToServer(new TestObject("From TestConnection"), TestConnectionKey.class);
		} catch (InterruptedException | ConnectionEstablishmentFailedException e) {
			e.printStackTrace();
		}

		clientStart.blockOnCurrentThread();
	}

	private static synchronized void print(ConnectionContext connectionContext, Session session, TestObject testObject) {
		System.out.println(++receivedCount + "] Received from " + connectionContext.getIdentifier().getSimpleName() + ": " + testObject);
	}
}
