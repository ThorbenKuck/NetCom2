package com.github.thorbenkuck.netcom2.integration.udp;

import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.integration.TestObject;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.client.Sender;

public class UDPClient {

	public static void main(String[] args) throws StartFailedException {
		ClientStart clientStart = ClientStart.udp("localhost", 4444);

		clientStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addFirst(System.out::println);

		clientStart.launch();

		Sender sender = Sender.open(clientStart);
		sender.objectToServer(new TestObject("From UDP"));
	}
}
