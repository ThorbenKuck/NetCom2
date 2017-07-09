package test.examples.live;

import de.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import de.thorbenkuck.netcom2.exceptions.StartFailedException;
import de.thorbenkuck.netcom2.logging.NetComLogging;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.server.ServerStart;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Server {

	private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
	private ServerStart serverStart;

	public Server() {
		serverStart = ServerStart.at(4444);
	}

	public static void main(String[] args) throws StartFailedException {
		NetComLogging.setLogging(Logging.disabled());
		new Server().start();
	}

	public void start() throws StartFailedException {
		serverStart.launch();

		register(serverStart);

		try {
			serverStart.acceptAllNextClients();
		} catch (ClientConnectionFailedException e) {
			e.printStackTrace();
		}
	}

	private void register(ServerStart serverStart) {

		serverStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addFirst((connection, session, testObject) -> System.out.println("Received " + testObject.getString() + " from Connection " + connection.getKey()));

	}

}
