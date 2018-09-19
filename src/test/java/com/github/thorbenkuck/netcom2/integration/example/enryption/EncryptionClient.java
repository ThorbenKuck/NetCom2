package com.github.thorbenkuck.netcom2.integration.example.enryption;

import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.integration.TestObject;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.client.Sender;

public class EncryptionClient {

	private final ClientStart clientStart;

	public EncryptionClient(String address, int port) {
		this.clientStart = ClientStart.at(address, port);
	}

	public static void main(String[] args) {
		EncryptionClient client = new EncryptionClient("localhost", 4444);
		try {
			client.run();
		} catch (StartFailedException e) {
			e.printStackTrace();
		}
	}

	public void run() throws StartFailedException {
		clientStart.addEncryptionAdapter(string -> Cipher.caesarEncryption(string, 12));
		clientStart.addDecryptionAdapter(string -> Cipher.caesarDecryption(string, 12));
		clientStart.launch();

		Sender sender = Sender.open(clientStart);

		while (true) {
			sender.objectToServer(new TestObject("Hi!"));
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
