package com.github.thorbenkuck.test;

import com.github.thorbenkuck.netcom2.auto.NetCom2;
import com.github.thorbenkuck.netcom2.auto.ObjectRepository;
import com.github.thorbenkuck.netcom2.auto.annotations.Register;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.client.Sender;
import com.github.thorbenkuck.netcom2.network.shared.Session;

public class Main {

	public Main(TestObject testObject) {
		System.out.println("Instantiated Main");
	}

	public static void main(String[] args) {
		ObjectRepository objectRepository = ObjectRepository.hashingRecursive();

		NetCom2.launchServer()
				.use(objectRepository)
				.at(8765)
				.onThread();

		ClientStart clientStart = NetCom2.launchClient()
				.use(objectRepository)
				.at("localhost", 8765)
				.get();

		Sender.open(clientStart).objectToServer(TestObject.create());
	}

	@Register
	public void foo(Session session, TestObject testObject) {
		System.out.println("Foo");
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		session.send(testObject);
	}

}
