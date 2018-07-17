package com.github.thorbenkuck.netcom2.integration.example.some;

import com.github.thorbenkuck.netcom2.TestObject;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.client.Sender;

import java.net.SocketException;

public class ServiceDiscoveryClient {

	public static void main(String[] args) {
		NetComLogging.setLogging(Logging.trace());
		ServiceDiscoveryClient discoveryClient = new ServiceDiscoveryClient();
		discoveryClient.startClient();
	}

	private void startClient() {
		try {
			ClientStart clientStart = ClientStart.findLocalServer(8888);

			try {
				clientStart.launch();
				Sender sender = Sender.open(clientStart);
				sender.objectToServer(new TestObject("Auto-Connected bitch!"));
				Thread.sleep(3000);
			} catch (StartFailedException e) {
				e.printStackTrace();
			}
		} catch (InterruptedException | SocketException e) {
			e.printStackTrace();
		}
	}

}
