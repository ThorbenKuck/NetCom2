package test.examples.tcp;

import de.thorbenkuck.netcom2.exceptions.StartFailedException;
import de.thorbenkuck.netcom2.logging.NetComLogging;
import de.thorbenkuck.netcom2.network.interfaces.ClientStart;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.clients.ConnectionFactory;
import de.thorbenkuck.netcom2.network.shared.clients.ConnectionFactoryHook;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import test.examples.TestObject;

public class TCPClientTest {

	public static void main(String[] args) {
		ConnectionFactory.setConnectionFactoryHook(ConnectionFactoryHook.tcp());
		NetComLogging.setLogging(Logging.trace());
		new TCPClientTest();
	}

	private ClientStart clientStart;
	private int count = 0;

	public TCPClientTest() {
		clientStart = ClientStart.at("localhost", 4545);
		register(clientStart.getCommunicationRegistration());

		try {
			System.out.println("Launching ..");
			clientStart.launch();
			System.out.println("LAUNCHED!");
		} catch (StartFailedException e) {
			e.printStackTrace();
			System.exit(1);
		}

		clientStart.send().objectToServer(new TestObject(Integer.toString(count++)));
		clientStart.send().objectToServer(new TestObject(Integer.toString(count++)));
		clientStart.send().objectToServer(new TestObject(Integer.toString(count++)));
		clientStart.send().objectToServer(new TestObject(Integer.toString(count++)));
		clientStart.send().objectToServer(new TestObject(Integer.toString(count++)));
		clientStart.send().objectToServer(new TestObject(Integer.toString(count++)));
		clientStart.send().objectToServer(new TestObject(Integer.toString(count++)));
		clientStart.send().objectToServer(new TestObject(Integer.toString(count++)));
		clientStart.send().objectToServer(new TestObject(Integer.toString(count++)));
	}

	private void register(CommunicationRegistration communicationRegistration) {
		communicationRegistration.register(TestObject.class)
				.addFirst(System.out::println);
	}
}
