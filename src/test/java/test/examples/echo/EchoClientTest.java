package test.examples.echo;

import de.thorbenkuck.netcom2.exceptions.StartFailedException;
import de.thorbenkuck.netcom2.network.client.Sender;
import de.thorbenkuck.netcom2.network.interfaces.ClientStart;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import test.examples.TestObject;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EchoClientTest {

	/**
	 * Used, to send TestObject every second to the Server
	 */
	private static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

	public static void main(String[] args) {
		// Create the ClientStart
		ClientStart clientStart = ClientStart.at("localhost", 8888);

		// Simply print out, what you received from the Server
		register(clientStart.getCommunicationRegistration());

		try {
			// Launch it
			clientStart.launch();
		} catch (StartFailedException e) {
			e.printStackTrace();
			System.exit(1);
		}
		// If launched, schedule the sending every 1 second
		schedule(clientStart.send());
	}

	private static void register(CommunicationRegistration communicationRegistration) {
		communicationRegistration.register(TestObject.class)
				.addFirst(((user, o) -> System.out.println("Server send: " + o.getHello())));
	}

	private static void schedule(Sender send) {
		scheduledExecutorService.scheduleAtFixedRate(() -> send.objectToServer(new TestObject("Hello!")), 1, 1, TimeUnit.SECONDS);
	}

}
