package com.github.thorbenkuck.netcom2.test.examples.rmi;

import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.network.interfaces.ClientStart;

public class RMIClient implements Runnable {

	private ClientStart clientStart = ClientStart.at("localhost", 4444);

	public static void main(String[] args) {
		new RMIClient().run();
	}

	@Override
	public void run() {
		try {
			clientStart.launch();
		} catch (StartFailedException e) {
			e.printStackTrace();
			return;
		}

		RemoteTestInterface test = clientStart.getRemoteObject(RemoteTestInterface.class);
		System.out.println(test.getHelloWorld());
	}
}
