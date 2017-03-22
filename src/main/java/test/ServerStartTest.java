package test;

import de.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import de.thorbenkuck.netcom2.exceptions.CommunicationAlreadySpecifiedException;
import de.thorbenkuck.netcom2.exceptions.StartFailedException;
import de.thorbenkuck.netcom2.network.server.ServerStart;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerStartTest {

	private static int port = 46091;
	private static ServerStart serverStart;
	private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

	public static void main(String[] args) {
		serverStart = ServerStart.of(port);
		serverStart.addClientConnectedHandler(client -> client.addDisconnectedHandler(client1 -> System.out.println(client1 + " disconnected! ABORT!")));

		try {
			register();
			new Thread(() -> {
				try {
					start();
				} catch (StartFailedException e) {
					e.printStackTrace();
				}
			}).start();
			scheduledExecutorService.scheduleAtFixedRate(() -> send(), 10, 2, TimeUnit.SECONDS);
		} catch (CommunicationAlreadySpecifiedException e) {
			e.printStackTrace();
		}
	}

	private static void register() throws CommunicationAlreadySpecifiedException {
		serverStart.getCommunicationRegistration().register(TestObject.class, (user, o) -> {
			System.out.println("received " + o.getHello() + " from " + user);
			user.send(new TestObject("World"));
		});
	}

	private static void start() throws StartFailedException {
		serverStart.launch();
		try {
			serverStart.acceptClients();
		} catch (ClientConnectionFailedException e) {
			throw new StartFailedException(e);
		}
	}

	private static void send() {
		System.out.println("Sending TestObjectTwo to registered Clients ..");
		serverStart.cache().addAndOverride(new TestObjectTwo(new Date().toString()));
	}
}
