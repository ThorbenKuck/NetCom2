package test.examples.general;

import de.thorbenkuck.netcom2.exceptions.StartFailedException;
import de.thorbenkuck.netcom2.logging.NetComLogging;
import de.thorbenkuck.netcom2.network.interfaces.ClientStart;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.Awaiting;
import de.thorbenkuck.netcom2.network.shared.cache.AbstractCacheObserver;
import de.thorbenkuck.netcom2.network.shared.cache.DeletedEntryEvent;
import de.thorbenkuck.netcom2.network.shared.cache.NewEntryEvent;
import de.thorbenkuck.netcom2.network.shared.cache.UpdatedEntryEvent;
import de.thorbenkuck.netcom2.network.shared.comm.model.Ping;
import test.examples.*;

import java.util.Observable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientStartTest {

	private static ClientStart clientStart;
	private static Logging logging = Logging.unified();
	private static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
	private static int port = 44444;

	public static void main(String[] args) {
		NetComLogging.setLogging(Logging.getDefault());
		clientStart = ClientStart.at("localhost", port);
//		clientStart.setServerSocketFactory((port, address) -> {
//			try {
//				return SSLSocketFactory.getDefault().createSocket(address, port);
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

		try {
			register();
			start();
			clientStart.send().objectToServer(new TestObject("This should not come back"));
			clientStart.send().registrationToServer(TestObjectTwo.class, new TestObserver());
			try {
				System.out.println("#1 Awaiting receive of Class TestObjectThree...");
				clientStart.send()
						.objectToServer(new Login())
						.andAwaitReceivingOfClass(TestObjectThree.class);
				System.out.println("#1 Received TestObjectThree.class!");
				clientStart.send().objectToServer(new Login());
				clientStart.send().objectToServer(new Login());
				clientStart.send().objectToServer(new Login());
				clientStart.send().objectToServer(new TestObject("THIS SHOULD COME BACK!"));
				Awaiting callBack = clientStart.createNewConnection(TestObject.class);
				System.out.println("SomeStuff");
				System.out.println("SomeMoreStuff");
				System.out.println("Jetzt warte ich auf die neue Connection..");
				callBack.synchronize();
				System.out.println("Connection wurde aufgebaut! JUHU!");
				System.out.println("Lass uns die neue Connection mal testen..");
				clientStart.send().objectToServer(new TestObject("Hello!"), TestObject.class).andAwaitReceivingOfClass(TestObject.class);
				System.out.println("Das lief doch gut!");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} catch (StartFailedException e) {
			e.printStackTrace();
		}
	}

	private static void register() {
		clientStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addLast((user, o) -> logging.info("Received " + o.getHello() + " from Server"));
		clientStart.getCommunicationRegistration()
				.register(TestObjectThree.class)
				.addLast((connection, user, o) -> logging.info("----\n" + o.getMsg() + "\n----"));

		clientStart.getCommunicationRegistration()
				.register(Ping.class)
				.addLast(ping -> logging.info("Received Ping from Server!"));
	}

	private static void start() throws StartFailedException {
		clientStart.addFallBackDeSerialization(new TestDeSerializer());
		clientStart.addFallBackSerialization(new TestSerializer());
		clientStart.addDisconnectedHandler(client -> {
			logging.info("Bye bye lieber Server");
			executorService.schedule(new ConnectionRetryRunnable(clientStart), 4, TimeUnit.SECONDS);
		});
		clientStart.launch();
	}

	private static class TestObserver extends AbstractCacheObserver {
		@Override
		public void newEntry(NewEntryEvent newEntryEvent, Observable observable) {
			logging.info("[NEW ENTRY] Received push from Server about: " + newEntryEvent.getObject());
		}

		@Override
		public void updatedEntry(UpdatedEntryEvent updatedEntryEvent, Observable observable) {
			logging.info("[UPDATE] Received push from Server about: " + updatedEntryEvent.getObject());
		}

		@Override
		public void deletedEntry(DeletedEntryEvent deletedEntryEvent, Observable observable) {
			logging.info("[DELETED] Received push from Server about: " + deletedEntryEvent.getCorrespondingClass());
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
				logging.info("Reconnected after " + tries + " tries!");
			} catch (StartFailedException e) {
				logging.catching(e);
				executorService.schedule(this, 4, TimeUnit.SECONDS);
			}
		}
	}
}