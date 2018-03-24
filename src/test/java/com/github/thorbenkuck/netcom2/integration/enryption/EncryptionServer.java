package com.github.thorbenkuck.netcom2.integration.enryption;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.integration.TestObject;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;

public class EncryptionServer {

	public EncryptionServer(int port) {
		this.serverStart = ServerStart.at(port);
	}

	public static void main(String[] args) {
		NetComLogging.setLogging(Logging.trace());
		EncryptionServer server = new EncryptionServer(4444);
		try {
			server.run();
		} catch (StartFailedException e) {
			e.printStackTrace();
		}
	}

	private final ServerStart serverStart;

	public void run() throws StartFailedException {
		serverStart.addClientConnectedHandler(client -> {
			client.setEncryptionAdapter(string -> Cipher.caesarEncryption(string, 12));
			client.setDecryptionAdapter(string -> Cipher.caesarDecryption(string, 12));
//			client.setMainSerializationAdapter(new TestObjectSerialization());
//			client.setMainDeSerializationAdapter(new TestObjectDeSerialization());
		});

		serverStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addFirst(o -> System.out.println(o.getHello()));

		serverStart.launch();
		try {
			serverStart.acceptAllNextClients();
		} catch (ClientConnectionFailedException e) {
			e.printStackTrace();
		}
	}
}
