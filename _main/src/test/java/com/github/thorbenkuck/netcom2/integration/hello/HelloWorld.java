package com.github.thorbenkuck.netcom2.integration.hello;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.client.Sender;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;

public class HelloWorld {

	public static void main(String[] args) {
		ClientStart clientStart = ClientStart.at("localhost", 8998);
		ServerStart serverStart = ServerStart.at(8998);

		serverStart.getCommunicationRegistration()
				.register(String.class)
				.addFirst(System.out::println);

		try {
			serverStart.launch();
			new Thread(() -> {
				try {
					serverStart.acceptAllNextClients();
				} catch (ClientConnectionFailedException e) {
					e.printStackTrace();
				}
			}).start();
			clientStart.launch();
			Sender.open(clientStart).objectToServer("Hello World");
		} catch (StartFailedException e) {
			e.printStackTrace();
		}
	}
}
