package com.github.thorbenkuck.netcom2.rmi;

import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.client.RemoteObjectFactory;

import static com.github.thorbenkuck.netcom2.rmi.RMIServer.SERVER_PREFIX;

public class RMIClient {

	private static final String INPUT = "RemoteRequest";

	public static void main(String[] args) {
		ClientStart clientStart = ClientStart.at("localhost", 4568);

		RemoteObjectFactory factory = RemoteObjectFactory.open(clientStart);

		try {
			clientStart.launch();
		} catch (StartFailedException e) {
			e.printStackTrace();
		}

		RemoteTestObject testObject = factory.create(RemoteTestObject.class);

		String result = testObject.convert(INPUT);
		System.out.println("Server Answer: \"" + result + "\"");

		if (!result.equals(SERVER_PREFIX + INPUT)) {
			throw new IllegalStateException("Test was not Successful!\nExpected: " + SERVER_PREFIX + INPUT + "\nReceived: " + result);
		}

		System.out.println("\n---\nTEST SUCCESSFUL\n---");
	}

}
