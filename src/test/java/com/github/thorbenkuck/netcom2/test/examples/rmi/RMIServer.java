package com.github.thorbenkuck.netcom2.test.examples.rmi;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;

public class RMIServer implements Runnable {

	private ServerStart serverStart = ServerStart.at(4444);

	public static void main(String[] args) {
		new RMIServer().run();
	}

	@Override
	public void run() {
		//serverStart.remoteObjects().hook(new RemoteTest());
		try {
			serverStart.launch();
			serverStart.acceptAllNextClients();
		} catch (StartFailedException | ClientConnectionFailedException e) {
			e.printStackTrace();
		}
	}

	private class RemoteTest implements RemoteTestInterface {

		@Override
		public String getHelloWorld() {
			throw new IllegalArgumentException("Bla");
		}
	}
}
