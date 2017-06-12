package test.examples.general;

import de.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import de.thorbenkuck.netcom2.exceptions.CommunicationAlreadySpecifiedException;
import de.thorbenkuck.netcom2.exceptions.StartFailedException;
import de.thorbenkuck.netcom2.logging.NetComLogging;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.server.ServerStart;
import de.thorbenkuck.netcom2.network.shared.Session;
import test.examples.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerStartTest {

	private static int port = 44444;
	private static Logging logging = Logging.unified();
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
		NetComLogging.setLogging(Logging.info());
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
			logging.info(stacktrace);
		});
	}

	private static void create() {
		serverStart = ServerStart.at(port);
		serverStart.addClientConnectedHandler(client -> {
			client.addFallBackDeSerialization(new TestDeSerializer());
			client.addFallBackSerialization(new TestSerializer());
			client.addDisconnectedHandler(client1 -> logging.info("ABORT!" + client1 + " disconnected!"));

			Session session = client.getSession();
			session.eventOf(Login.class)
					.addFirst(login -> logging.info("Du bist doch schon eingeloggt, du eumel!"))
					.withRequirement(login -> session.isIdentified());

			session.eventOf(Login.class)
					.addLast(login -> {
						logging.info("Okay, ich logge dich ein...");
						session.setIdentified(true);
					}).withRequirement(login -> ! session.isIdentified());


//			HeartBeat<Session> heartBeat = HeartBeatFactory.get().produce();
//
//			heartBeat.configure()
//					.tickRate()
//					.times(1)
//					.in(1, TimeUnit.SECONDS)
//					.and()
//					.run()
//					.setAction(currentSession -> currentSession.send(new Ping()));
//
//			session.addHeartBeat(heartBeat);
		});
//		serverStart.setSocketFactory(integer -> {
//			try {
//				return SSLServerSocketFactory.getDefault().createServerSocket(integer);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			try {
//				return new ServerSocket(integer);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			return null;
//		});
	}

	private static void schedule() {
		scheduledExecutorService.scheduleAtFixedRate(ServerStartTest::updateCache, 10, 10, TimeUnit.SECONDS);
		scheduledExecutorService.scheduleAtFixedRate(ServerStartTest::send, 11, 11, TimeUnit.SECONDS);
	}

	private static void register() throws CommunicationAlreadySpecifiedException {
		serverStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addLast((session, o) -> logging.info("------received " + o.getHello() + " from " + session + "-------"))
				.withRequirement(Session::isIdentified);
		serverStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addLast((connection, session, o) -> connection.offerToSend(new TestObject("World")))
				.withRequirement(Session::isIdentified);

		serverStart.getCommunicationRegistration()
				.register(Login.class)
				.addLast((session, o) ->
						session.triggerEvent(Login.class, o)
				);

		serverStart.getCommunicationRegistration()
				.addDefaultCommunicationHandler(object -> logging.error("Ich kenne das Object: " + object.getClass() + " nicht!"));
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
