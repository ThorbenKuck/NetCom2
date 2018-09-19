package com.github.thorbenkuck.netcom2.integration.rmi;

import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.client.RemoteObjectFactory;

import static com.github.thorbenkuck.netcom2.integration.rmi.RMIServer.SERVER_PREFIX;

public class RMIClient {

	private static final String INPUT = "RemoteRequest";
	private static final int REPEAT_OF_TEST = 100;

	public static void main(String[] args) {
		long startOfConstructionTime = System.currentTimeMillis();
		ClientStart clientStart = ClientStart.at("localhost", 4568);

		RemoteObjectFactory factory = RemoteObjectFactory.open(clientStart);

		try {
			clientStart.launch();
		} catch (StartFailedException e) {
			e.printStackTrace();
		}
		long endOfConstructionTime = System.currentTimeMillis();
		System.out.println("Test Constructed in " + (endOfConstructionTime - startOfConstructionTime) + " milliseconds");
		int count = 0;
		String result = "";
		while (count < REPEAT_OF_TEST) {
			long startTime = System.currentTimeMillis();

			RemoteTestObject testObject = factory.create(RemoteTestObject.class);

			result = testObject.convert(INPUT);
			long stopTime = System.currentTimeMillis();
			if (count == 0) {
				System.out.println((count + 1) + "): Server Answer: \"" + result + "\"");
			}
			System.out.println((count + 1) + "): Execution took " + (stopTime - startTime) + " milliseconds to Execute");
			++count;
		}

		if (!result.equals(SERVER_PREFIX + INPUT)) {
			throw new IllegalStateException("Test was not Successful!\nExpected: " + SERVER_PREFIX + INPUT + "\nReceived: " + result);
		}

		System.out.println("\n---\nTEST SUCCESSFUL\n---");
	}

}
