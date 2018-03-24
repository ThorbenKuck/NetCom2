package com.github.thorbenkuck.netcom2.integration.enryption;

import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.integration.TestObject;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;

public class EncryptionClient {

	private final ClientStart clientStart;

	public static void main(String[] args)  {
		EncryptionClient client = new EncryptionClient("localhost", 4444);
		try {
			client.run();
		} catch (StartFailedException e) {
			e.printStackTrace();
		}
	}

	public EncryptionClient(String address, int port) {
		this.clientStart = ClientStart.at(address, port);
	}

	public void run() throws StartFailedException {
		clientStart.setEncryptionAdapter(string -> Cipher.caesarEncryption(string, 12));
		clientStart.setDecryptionAdapter(string -> Cipher.caesarDecryption(string, 12));
		clientStart.launch();

		while(true) {
			clientStart.send()
					.objectToServer(new TestObject("Hi!"));
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
