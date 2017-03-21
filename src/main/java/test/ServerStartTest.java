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
	private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);

	public static void main(String[] args) throws StartFailedException, ClientConnectionFailedException, CommunicationAlreadySpecifiedException {
		serverStart = ServerStart.of(port);

		register();
		start();

		scheduledExecutorService.scheduleAtFixedRate(ServerStartTest::send, 3, 1, TimeUnit.SECONDS);
	}

	private static void register() throws CommunicationAlreadySpecifiedException {
		serverStart.getCommunicationRegistration().register(TestObject.class, (user, o) -> {
			System.out.println("received " + o.getHello() + " from " + user);
			user.send(new TestObject("World"));
		});
	}

	private static void start() throws StartFailedException {
		serverStart.launch();
		new Thread(() -> {
			try {
				serverStart.acceptClients();
			} catch (ClientConnectionFailedException e) {
				e.printStackTrace();
			}
		}).start();
	}

	private static void send() {
		System.out.println("Sending TestObjectTwo to registered Clients ..");
		serverStart.cache().addAndOverride(new TestObjectTwo(new Date().toString()));
	}
}
