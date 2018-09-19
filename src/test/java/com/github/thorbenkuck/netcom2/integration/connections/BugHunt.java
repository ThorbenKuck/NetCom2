package com.github.thorbenkuck.netcom2.integration.connections;

import com.github.thorbenkuck.keller.sync.Synchronize;
import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.ConnectionEstablishmentFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.client.Sender;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.connections.ConnectionContext;

public class BugHunt {

	public static void main(String[] args) {
		new BugHunt().test();
	}

	private void clientReceived(ConnectionContext connectionContext, Session session, BugObject bugObject) {
		System.out.println("[Client] Received BugObject from " + connectionContext.getIdentifier());
	}

	private void serverReceived(ConnectionContext connectionContext, Session session, BugObject bugObject) {
		System.out.println("[Server] Received BugObject from " + connectionContext.getIdentifier());
		session.send(bugObject);
	}

	public void test() {
		ServerStart serverStart = ServerStart.at(7899);
		ClientStart clientStart = ClientStart.at("localhost", 7899);

		serverStart.getCommunicationRegistration()
				.register(BugObject.class)
				.addFirst(this::serverReceived);

		clientStart.getCommunicationRegistration()
				.register(BugObject.class)
				.addFirst(this::clientReceived);

		try {
			serverStart.launch();
			Synchronize threadStarted = Synchronize.createDefault();
			new Thread(() -> {
				try {
					threadStarted.goOn();
					serverStart.acceptAllNextClients();
				} catch (ClientConnectionFailedException e) {
					e.printStackTrace();
				}
			}).start();
			threadStarted.synchronize();

			clientStart.launch();
			Thread.sleep(100);
		} catch (StartFailedException | InterruptedException e) {
			e.printStackTrace();
			return;
		}

		Sender sender = Sender.open(clientStart);
		sender.objectToServer(new BugObject());

		try {
			clientStart.newConnection(TestConnection.class).synchronize();
			System.out.println("New Connection established");
		} catch (InterruptedException | ConnectionEstablishmentFailedException e) {
			e.printStackTrace();
			return;
		}

		System.out.println("Sending BugObject over new Connection");
		sender.objectToServer(new BugObject(), TestConnection.class);
		System.out.println("Done! The Bug Object was send, but was it received?");
	}

	private class TestConnection {

	}

}
