package com.github.thorbenkuck.netcom2.test.examples;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;

public class Server {

	private static int port = 4444;

	public static void main(String[] args) {
		ServerStart serverStart = ServerStart.at(port);

		ServerTestImplementation serverTestImplementation = new ServerTestImplementation();
		serverStart.remoteObjects()
				.hook(serverTestImplementation);

		try {
			serverStart.launch();
			serverStart.acceptAllNextClients();
		} catch (StartFailedException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (ClientConnectionFailedException e) {
			e.printStackTrace();
		}
	}

	static class ServerTestImplementation implements TestInterface {
		@Override
		public String compute(String input) {
			return "------\n" + input + "\n------";
		}
	}

}
