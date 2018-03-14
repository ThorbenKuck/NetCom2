package com.github.thorbenkuck.netcom2.integration.general;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.integration.*;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.ClientConnectedHandler;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerStartTest {

	private static final LoggingHandler loggingHandler = new LoggingHandler();
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
		NetComLogging.setLogging(Logging.trace());
		catching();
		create();
		schedule();
		register();
		starter.start();
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
		serverStart = ServerStart.at(port);
		serverStart.addClientConnectedHandler(client -> {
			loggingHandler.handle(client);
			client.addFallBackDeSerialization(new TestDeSerializer());
			client.addFallBackSerialization(new TestSerializer());
			client.addDisconnectedHandler(client1 -> System.out.println("ABORT!" + client1 + " disconnected!"));
			Session session = client.getSession();
			session.eventOf(Login.class)
					.addFirst(login -> System.out.println("Du bist doch schon eingeloggt, du eumel!"))
					.withRequirement(login -> session.isIdentified());

			session.eventOf(Login.class)
					.addLast(login -> {
						System.out.println("Okay, ich logge dich ein...");
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
//		serverStart.setServerSocketFactory(integer -> {
//			String keystoreFileName = "keystore.jks";
//			char[] password = "password".toCharArray();
//			String alias = "test";
//
//
//			try {
//				FileInputStream fileInputStream = new FileInputStream(keystoreFileName);
//				KeyStore keyStore = KeyStore.getInstance("JKS");
//				keyStore.load(fileInputStream, password);
//
//				ServerSocketFactory sslServerSocketFactory = SSLServerSocketFactory.getDefault();
//				return sslServerSocketFactory.createServerSocket(integer);
//			} catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
//				e.printStackTrace();
//			}
//
//			System.out.println("No SSL for you!");
//
//			try {
//				return new ServerSocket(integer);
//			} catch (IOException e) {
//				e.printStackTrace();
//				return null;
//			}
//		});
	}

	private static void schedule() {
		scheduledExecutorService.scheduleAtFixedRate(ServerStartTest::updateCache, 10, 10, TimeUnit.SECONDS);
		scheduledExecutorService.scheduleAtFixedRate(ServerStartTest::send, 11, 11, TimeUnit.SECONDS);
	}

	private static void register() {
		serverStart.remoteObjects().hook(new SuperTest());
		serverStart.remoteObjects().register(new SuperTest());
		serverStart.remoteObjects().register(new SuperTest(), Test.class, ReturnTest.class);

		serverStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addLast((session, o) -> System.out.println("------received " + o.getHello() + " from " + session + "-------"))
				.withRequirement(Session::isIdentified);
		serverStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addFirst((connection, session, o) -> System.out.println("TestObject" + connection + " can be executed " + session.isIdentified()));
		serverStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addLast((connection, session, o) -> connection.write(new TestObject(connection.getKey() + ":" + o.getHello())))
				.withRequirement(Session::isIdentified);
		serverStart.getCommunicationRegistration()
				.register(Login.class)
				.addLast((session, o) -> session.triggerEvent(Login.class, o));

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

	private static class LoggingHandler implements ClientConnectedHandler {

		private Logging logging = Logging.unified();

		@Override
		public void handle(Client client) {
			logging.debug(client.getFormattedAddress());
		}
	}

	private static class SuperTest implements Test, ReturnTest, ParameterReturnTest {

		@Override
		public String getReturnValue() {
			return "This is remotely generated by a call of ReturnTest.class";
		}

		@Override
		public void fire() {
			serverStart.distribute().toAll(new MessageFromServer("This is send because of the remote call of Test.fire()"));
		}

		@Override
		public String concatAndReturn(String original) {
			return "Server says: " + original + " is okay! :)";
		}
	}
}
