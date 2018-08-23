package com.github.thorbenkuck.netcom2.integration.example.some;

import com.github.thorbenkuck.netcom2.TestObject;
import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.Session;

import java.net.SocketException;

public class ServiceDiscoveryServer {

	public static void main(String[] args) {
		NetComLogging.setLogging(Logging.trace());
		ServiceDiscoveryServer serviceDiscoveryServer = new ServiceDiscoveryServer();
		serviceDiscoveryServer.startServer();
	}

	private void printTestObject(TestObject testObject) {
		System.out.println("Received Message vom Client: " + testObject);
	}

	private void startServer() {
		ServerStart serverStart = ServerStart.at(4546);
		serverStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addFirst(this::printTestObject);

		serverStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addFirst(Session::send);
		try {
			serverStart.launch();
			serverStart.allowLocalAreaNetworkFind(8888);

			new Thread(() -> {
				try {
					serverStart.acceptAllNextClients();
				} catch (ClientConnectionFailedException e) {
					e.printStackTrace(System.out);
				}
			}).start();
		} catch (StartFailedException | SocketException e) {
			e.printStackTrace();
		}
	}
}
