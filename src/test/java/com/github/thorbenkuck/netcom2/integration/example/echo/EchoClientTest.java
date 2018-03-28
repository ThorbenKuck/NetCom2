package com.github.thorbenkuck.netcom2.integration.example.echo;

import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.integration.TestObject;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.client.Sender;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EchoClientTest {

	/**
	 * Used, to send TestObject every second to the Server
	 */
	private static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
	private static ClientStart clientStart;

	public static void main(String[] args) {
		// Create the ClientStart
		clientStart = ClientStart.at("localhost", 8888);

		// Simply print out, what you received from the Server
		register(clientStart.getCommunicationRegistration());

		try {
			// Launch it
			clientStart.launch();
		} catch (StartFailedException e) {
			e.printStackTrace();
			System.exit(1);
		}
		// If launched, schedule the sending every 1 second
		schedule(clientStart.send());
	}

	private static void register(CommunicationRegistration communicationRegistration) {
		communicationRegistration.register(TestObject.class)
				.addFirst((testObject -> System.out.println("Server send: " + testObject.getHello())));
	}

	private static void schedule(Sender send) {
		scheduledExecutorService.scheduleAtFixedRate(() -> {
			send.objectToServer(new TestObject("Hello!"));
			System.out.println("Sending Hello! to server ...");
		}, 1, 1, TimeUnit.SECONDS);
	}
}
