package com.github.thorbenkuck.netcom2.integration.example.some;

import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.integration.TestObject;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.client.Sender;

import java.net.SocketException;

public class ServiceDiscoveryClient {

	private boolean received = false;

	public static void main(String[] args) {
		ServiceDiscoveryClient discoveryClient = new ServiceDiscoveryClient();
		discoveryClient.startClient();
	}

	private void received(TestObject testObject) {
		received = true;
		System.out.println(testObject);
	}

	private void startClient() {
		try {
			ClientStart clientStart = ClientStart.findLocalServer(8888);

			clientStart.getCommunicationRegistration()
					.register(TestObject.class)
					.addFirst(this::received);

			try {
				clientStart.launch();
				Sender sender = Sender.open(clientStart);
				sender.objectToServer(new TestObject("Auto-Connected bitch!"));
				Thread.sleep(10000);
				if (!received) {
					throw new IllegalStateException("Test was not Successful!\nExpected a ping back but got none");
				}

				System.out.println("\n---\nTEST SUCCESSFUL\n---");
			} catch (StartFailedException e) {
				e.printStackTrace();
			}
		} catch (InterruptedException | SocketException e) {
			e.printStackTrace();
		}
	}

}
