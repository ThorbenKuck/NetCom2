package com.github.thorbenkuck.test;

import com.github.thorbenkuck.netcom2.auto.NetCom2;
import com.github.thorbenkuck.netcom2.auto.ObjectRepository;
import com.github.thorbenkuck.netcom2.auto.annotations.Configure;
import com.github.thorbenkuck.netcom2.auto.annotations.Connect;
import com.github.thorbenkuck.netcom2.auto.annotations.Disconnect;
import com.github.thorbenkuck.netcom2.auto.annotations.Register;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;

public class ServerMain {

	public ServerMain(TestObject testObject) {
		System.out.println("Instantiated ServerMain");
	}

	public static void main(String[] args) {
		TestObject testObject = TestObject.create();
		ObjectRepository objectRepository = ObjectRepository.hashingRecursive();
		objectRepository.add(testObject);

		NetCom2.launchServer()
				.use(objectRepository)
				.at(8765)
				.onCurrentThread();
	}

	@Register
	public void foo(Session session, TestObject testObject) {
		System.out.println("[OK]{Server} Received");
		session.send(testObject);
	}

	@Connect
	public void connect(Client client) {
		System.out.println("[OK]{Server} Connected");
	}

	@Disconnect
	public void disconnect(Client client) {
		System.out.println("[OK]{Server} Disconnect");
	}

	@Configure
	public void configure(ServerStart serverStart) {
		System.out.println("[OK]{Server} Configuring");
	}

}
