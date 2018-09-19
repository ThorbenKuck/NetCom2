package com.github.thorbenkuck.netcom2.integration.example.enryption;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.integration.TestObject;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.clients.ObjectHandler;

public class EncryptionServer {

	private final ServerStart serverStart;

	public EncryptionServer(int port) {
		this.serverStart = ServerStart.at(port);
	}

	public static void main(String[] args) {
		EncryptionServer server = new EncryptionServer(4444);
		try {
			server.run();
		} catch (StartFailedException e) {
			e.printStackTrace();
		}
	}

	public void run() throws StartFailedException {
		serverStart.addClientConnectedHandler(client -> {
			ObjectHandler handler = client.objectHandler();
			handler.addEncryptionAdapter(string -> Cipher.caesarEncryption(string, 12));
			handler.addDecryptionAdapter(string -> Cipher.caesarDecryption(string, 12));
		});

		serverStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addFirst(o -> System.out.println(o.getContent()));

		serverStart.launch();
		try {
			serverStart.acceptAllNextClients();
		} catch (ClientConnectionFailedException e) {
			e.printStackTrace();
		}
	}
}
