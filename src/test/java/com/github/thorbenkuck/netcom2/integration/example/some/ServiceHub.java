package com.github.thorbenkuck.netcom2.integration.example.some;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.services.ServiceDiscoveryHub;

import java.net.SocketException;

public class ServiceHub {

	public static void main(String[] args) throws StartFailedException {
		ServerStart serverStart = ServerStart.at(4444);
		serverStart.addClientConnectedHandler(System.out::println);
		serverStart.launch();

		long startTime = System.currentTimeMillis();

		new Thread(() -> {
			try {
				serverStart.acceptAllNextClients();
			} catch (ClientConnectionFailedException e) {
				e.printStackTrace();
			}
		}).start();

		ServiceDiscoveryHub hub = ServiceDiscoveryHub.create(8888);
		hub.addHeaderEntry(header -> header.addEntry("TIME_RUNNING", Long.toString(System.currentTimeMillis() - startTime)));
		hub.connect(serverStart);

		try {
			hub.listen();
		} catch (SocketException e) {
			e.printStackTrace();
		}

		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
