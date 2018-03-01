package com.github.thorbenkuck.netcom2.test.examples.rmi;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

public class RMIServer implements Runnable {

	private ServerStart serverStart = ServerStart.at(666);

	public static void main(String[] args) {
		new RMIServer().run();
	}

	@Override
	public void run() {
		serverStart.remoteObjects().register(new RemoteTest(), RemoteTestInterface.class);
		try {
			serverStart.launch();
			NetCom2Utils.runLater(() -> {
				try {
					serverStart.acceptAllNextClients();
				} catch (ClientConnectionFailedException e) {
					e.printStackTrace();
				}
			});
		} catch (StartFailedException e) {
			e.printStackTrace();
		}

		System.out.println();
	}

	private class RemoteTest implements RemoteTestInterface {

		@Override
		public String getHelloWorld() {
			return "Fick dich, frag doch wen anders!";
		}
	}
}
