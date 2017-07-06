package test.examples.general;

import de.thorbenkuck.netcom2.exceptions.StartFailedException;
import de.thorbenkuck.netcom2.logging.NetComLogging;
import de.thorbenkuck.netcom2.network.interfaces.ClientStart;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.Awaiting;
import de.thorbenkuck.netcom2.network.shared.cache.*;
import de.thorbenkuck.netcom2.network.shared.comm.model.Ping;
import test.examples.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientStartTest {

	private static ClientStart clientStart;
	private static Logging logging = Logging.unified();
	private static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
	private static int port = 44444;

	public static void main(String[] args) {
		NetComLogging.setLogging(Logging.trace());
		create();
		setup();

		try {
			start();
			send();
		} catch (StartFailedException e) {
			e.printStackTrace();
		}
	}

	private static void create() {
		clientStart = ClientStart.at("localhost", port);
	}

	private static void setup() {
//		clientStart.setSocketFactory((port, address) -> {
//			try {
//				return SSLSocketFactory.getDefaultJavaSerialization().createSocket(address, port);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			try {
//				return new Socket(address, port);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			return null;
//		});
		register();
	}

	private static void start() throws StartFailedException {
		clientStart.addFallBackDeSerialization(new TestDeSerializer());
		clientStart.addFallBackSerialization(new TestSerializer());
		clientStart.addDisconnectedHandler(client -> {
			System.out.println("Bye bye dear Server");
			executorService.schedule(new ConnectionRetryRunnable(clientStart), 4, TimeUnit.SECONDS);
		});
		clientStart.launch();
	}

	private static void send() {
		clientStart.send().objectToServer(new TestObject("This should not come back"));
		clientStart.send().registrationToServer(TestObjectTwo.class, new TestObserver());
		try {
			System.out.println("#1 Awaiting receive of Class TestObjectThree...");
			clientStart.send()
					.objectToServer(new Login())
					.andWaitFor(TestObjectThree.class);
			System.out.println("#1 Received TestObjectThree.class!");
			clientStart.send().objectToServer(new Login());
			clientStart.send().objectToServer(new Login());
			clientStart.send().objectToServer(new Login());
			clientStart.send().objectToServer(new TestObject("THIS SHOULD COME BACK!"));
			Awaiting callBack = clientStart.createNewConnection(TestObject.class);
			System.out.println("SomeStuff");
			System.out.println("SomeMoreStuff");
			System.out.println("Now wait for the new Connection..");
			callBack.synchronize();
			System.out.println("Connection established! YAY!");
			System.out.println("Let's test the new Connection ..");
			clientStart.send().objectToServer(new TestObject("Hello!"), TestObject.class).andWaitFor(TestObject.class);
			System.out.println("That was good, was'nt it?");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void register() {
		clientStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addLast(o -> System.out.println("Received " + o.getHello() + " from Server"));
		clientStart.getCommunicationRegistration()
				.register(TestObjectThree.class)
				.addLast(o -> System.out.println("----\n" + o.getMsg() + "\n----"));

		clientStart.getCommunicationRegistration()
				.register(Ping.class)
				.addLast(ping -> System.out.println("Received Ping from Server!"));
	}

	private static class TestObserver extends AbstractCacheObserver<TestObjectTwo> {
		private TestObserver() {
			super(TestObjectTwo.class);
		}

		@Override
		public void newEntry(TestObjectTwo testObjectTwo, CacheObservable observable) {
			System.out.println("[NEW ENTRY] Received push from Server about: " + testObjectTwo);
		}

		@Override
		public void updatedEntry(TestObjectTwo testObjectTwo, CacheObservable observable) {
			System.out.println("[UPDATE] Received push from Server about: " + testObjectTwo);
		}

		@Override
		public void deletedEntry(TestObjectTwo testObjectTwo, CacheObservable observable) {
			System.out.println("[DELETED] Received push from Server about: " + testObjectTwo);
		}
	}

	private static class ConnectionRetryRunnable implements Runnable {

		private ClientStart clientStart;
		private int tries = 0;

		private ConnectionRetryRunnable(ClientStart clientStart) {
			this.clientStart = clientStart;
		}

		@Override
		public void run() {
			++ tries;
			try {
				clientStart.launch();
				System.out.println("Reconnected after " + tries + " tries!");
				send();
			} catch (StartFailedException e) {
				logging.catching(e);
				executorService.schedule(this, 4, TimeUnit.SECONDS);
			}
		}
	}
}