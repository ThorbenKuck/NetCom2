package com.github.thorbenkuck.netcom2.integration.nio;

import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.integration.TestObject;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.modules.ModuleFactory;

public class NIOClient {

	private final ClientStart clientStart;

	public NIOClient(String address, int port) {
		this.clientStart = ClientStart.at(address, port);
	}

	public static void main(String[] args) {
		NetComLogging.setLogging(Logging.trace());
		NIOClient client = new NIOClient("localhost", 4444);
		client.run();
	}

	public void run() {
		ModuleFactory.access()
				.createNIO()
				.applyTo(clientStart);

		clientStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addFirst(testObject -> System.out.println(testObject.getHello()));

		try {
			clientStart.launch();
		} catch (StartFailedException e) {
			e.printStackTrace();
		}

		clientStart.send()
				.objectToServer(new TestObject("This is NIO! BITCH!"));

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
