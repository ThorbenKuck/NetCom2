package test;

import de.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import de.thorbenkuck.netcom2.exceptions.CommunicationAlreadySpecifiedException;
import de.thorbenkuck.netcom2.exceptions.StartFailedException;
import de.thorbenkuck.netcom2.network.server.ServerStart;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerStartTest {

	private static int port = 44444;
	private static ServerStart serverStart;
	private static Thread starter = new Thread(() -> {
		try {
			start();
		} catch (StartFailedException e) {
			System.exit(1);
		}
	});
	private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

	public static void main(String[] args) {
		catching();
		create();
		schedule();
		try {
			register();
			starter.start();
		} catch (CommunicationAlreadySpecifiedException e) {
			throw new RuntimeException(e);
		}
	}

	private static void catching() {
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String stacktrace = sw.toString();
			System.out.println(stacktrace);
		});
	}

	private static void create() {
		serverStart = ServerStart.of(port);
		serverStart.addClientConnectedHandler(client -> client.addDisconnectedHandler(client1 -> System.out.println("ABORT!" + client1 + " disconnected!")));
	}

	private static void schedule() {
		scheduledExecutorService.scheduleAtFixedRate(ServerStartTest::updateCache, 10, 10, TimeUnit.SECONDS);
		scheduledExecutorService.scheduleAtFixedRate(ServerStartTest::send, 11, 11, TimeUnit.SECONDS);
	}

	private static void register() throws CommunicationAlreadySpecifiedException {
		serverStart.getCommunicationRegistration().register(TestObject.class, (user, o) -> {
			System.out.println("received " + o.getHello() + " from " + user);
			user.send(new TestObject("World"));
		});

		serverStart.getCommunicationRegistration().register(Login.class, ((user, o) -> user.setIdentified(true)));
	}

	private static void start() throws StartFailedException {
		serverStart.launch();
		try {
			serverStart.acceptAllNextClients();
		} catch (ClientConnectionFailedException e) {
			throw new StartFailedException(e);
		}
	}

	private static void updateCache() {
		serverStart.cache().addAndOverride(new TestObjectTwo(new Date().toString()));
	}

	private static void send() {
		serverStart.distribute().toAll(new TestObjectThree("It is now: " + LocalDateTime.now().toString()));
	}
}
