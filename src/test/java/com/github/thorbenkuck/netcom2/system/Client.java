package com.github.thorbenkuck.netcom2.system;

import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;

public class Client {

	private static int port = 4444;
	private static String address = "localhost";

	public static void main(String[] args) {
		ClientStart clientStart = ClientStart.at(address, port);

		try {
			clientStart.launch();
		} catch (StartFailedException e) {
			e.printStackTrace();
		}

		TestInterface testInterface = clientStart.getRemoteObject(TestInterface.class);
		System.out.println(testInterface.compute("Test"));
	}

}
