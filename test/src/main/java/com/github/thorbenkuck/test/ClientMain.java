package com.github.thorbenkuck.test;

import com.github.thorbenkuck.netcom2.auto.ObjectRepository;
import com.github.thorbenkuck.netcom2.auto.annotations.Register;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.client.Sender;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.test.generated.ClientOnlyRegistration;

public class ClientMain {

	private static ClientStart clientStart;

	public ClientMain(TestObject testObject) {
		System.out.println("Instantiated ClientMain");
	}

	public static void main(String[] args) throws StartFailedException {
		TestObject testObject = TestObject.create();
		ObjectRepository objectRepository = ObjectRepository.hashingRecursive();
		objectRepository.add(testObject);

		clientStart = ClientStart.at("localhost", 8765);
		new ClientOnlyRegistration().apply(clientStart, objectRepository);
		clientStart.launch();

		Sender.open(clientStart).objectToServer(testObject);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Register(name = "ClientOnlyRegistration", autoLoad = false)
	public void foo(Session session, TestObject testObject) {
		System.out.println("[OK]{Client} Received");
		clientStart.softStop();
	}
}
