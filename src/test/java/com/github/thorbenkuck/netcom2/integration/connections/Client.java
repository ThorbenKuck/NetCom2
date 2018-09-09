package com.github.thorbenkuck.netcom2.integration.connections;

import com.github.thorbenkuck.keller.sync.Awaiting;
import com.github.thorbenkuck.netcom2.exceptions.ConnectionEstablishmentFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.integration.TestObject;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.client.Sender;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.connections.ConnectionContext;

public class Client {

	private static int receivedCount = 0;

	public static void main(String[] args) {
		NetComLogging.setLogging(Logging.disabled());
		ClientStart clientStart = ClientStart.at("localhost", 4569);

		clientStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addFirst(Client::print);

		Awaiting awaiting;
		try {
			clientStart.launch();
			awaiting = clientStart.newConnection(TestConnectionKey.class);
		} catch (StartFailedException | ConnectionEstablishmentFailedException e) {
			e.printStackTrace();
			return;
		}

		Sender sender = Sender.open(clientStart);

		sender.objectToServer(new TestObject("From DefaultConnection"));

		try {
			awaiting.synchronize();
			System.out.println("New Connection established");
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
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		clientStart.blockOnCurrentThread();
	}

	private static synchronized void print(ConnectionContext connectionContext, Session session, TestObject testObject) {
		System.out.println(++receivedCount + "] Received from " + connectionContext.getIdentifier().getSimpleName() + ": " + testObject);
	}
}
