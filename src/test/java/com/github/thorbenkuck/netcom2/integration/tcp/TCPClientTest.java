package com.github.thorbenkuck.netcom2.integration.tcp;

import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.integration.TestObject;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import org.junit.Ignore;

@Ignore
public class TCPClientTest {

	public TCPClientTest() {
		final ClientStart clientStart = ClientStart.at("localhost", 4545);
		register(clientStart.getCommunicationRegistration());

		try {
			System.out.println("Launching ..");
			clientStart.launch();
			System.out.println("LAUNCHED!");
		} catch (StartFailedException e) {
			e.printStackTrace();
			System.exit(1);
		}

		int count = 0;
		clientStart.send().objectToServer(new TestObject(Integer.toString(++count)));
		clientStart.send().objectToServer(new TestObject(Integer.toString(++count)));
		clientStart.send().objectToServer(new TestObject(Integer.toString(++count)));
		clientStart.send().objectToServer(new TestObject(Integer.toString(++count)));
		clientStart.send().objectToServer(new TestObject(Integer.toString(++count)));
		clientStart.send().objectToServer(new TestObject(Integer.toString(++count)));
		clientStart.send().objectToServer(new TestObject(Integer.toString(++count)));
		clientStart.send().objectToServer(new TestObject(Integer.toString(++count)));
		clientStart.send().objectToServer(new TestObject(Integer.toString(++count)));
	}

	public static void main(String[] args) {
		NetComLogging.setLogging(Logging.trace());
		new TCPClientTest();
	}

	private void register(CommunicationRegistration communicationRegistration) {
		communicationRegistration.register(TestObject.class)
				.addFirst(System.out::println);
	}
}
