package com.github.thorbenkuck.netcom2.tcp;

import com.github.thorbenkuck.netcom2.TestObject;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.client.Sender;

import static com.github.thorbenkuck.netcom2.tcp.TCPServer.TCP_TEST_PORT;

public class TCPClient {

	private static int receivedCount = 0;

	public static void main(String[] args) throws StartFailedException, InterruptedException {
		NetComLogging.setLogging(Logging.trace());
		ClientStart clientStart = ClientStart.tcp("localhost", TCP_TEST_PORT);
		clientStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addFirst(TCPClient::received);
		clientStart.launch();

		final int toSend = 5;
		int count = 0;

		Sender sender = Sender.open(clientStart);
		while (count < toSend) {
			sender.objectToServer(new TestObject("Over TCP-Connection"));
			++count;
		}
		Thread.sleep(3000);

		if (receivedCount != toSend) {
			throw new IllegalStateException("Test was not Successful!\nExpected: " + toSend + "\nReceived:" + receivedCount);
		}

		System.out.println("\n---\nTEST SUCCESSFUL\n---");
	}

	private static synchronized void received(TestObject testObject) {
		++receivedCount;
		System.out.println(testObject);
	}

}
