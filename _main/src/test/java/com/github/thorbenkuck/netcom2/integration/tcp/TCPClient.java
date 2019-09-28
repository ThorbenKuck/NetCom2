package com.github.thorbenkuck.netcom2.integration.tcp;

import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.integration.TestObject;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.client.Sender;

import java.util.concurrent.TimeUnit;

import static com.github.thorbenkuck.netcom2.integration.tcp.TCPServer.TCP_TEST_PORT;

public class TCPClient {

	private static int receivedCount = 0;
	private static final long timeLimit = TimeUnit.MILLISECONDS.toMillis(500);

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
		Thread.sleep(timeLimit);

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
