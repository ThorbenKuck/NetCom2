package com.github.thorbenkuck.netcom2.test.examples.general;

import com.github.thorbenkuck.netcom2.annotations.ReceiveHandler;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.ClientStart;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.cache.AbstractCacheObserver;
import com.github.thorbenkuck.netcom2.network.shared.cache.CacheObservable;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.test.examples.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientStartTest {

	private ClientStart clientStart;
	private Logging logging = Logging.unified();
	private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
	private int port = 44444;

	public static void main(String[] args) {
		new ClientStartTest().run();
	}

	public void run() {
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

	private void create() {
		clientStart = ClientStart.at("localhost", port);
	}

	private void setup() {
//		clientStart.setSocketFactory((port, address) -> {
//			try {
//				return SSLSocketFactory.getDefaultJavaSerialization().createSocket(address, port);
//			} catch (IOExceptiSealingon e) {
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

	private void start() throws StartFailedException {
		clientStart.addFallBackDeSerialization(new TestDeSerializer());
		clientStart.addFallBackSerialization(new TestSerializer());
		clientStart.addDisconnectedHandler(client -> {
			System.out.println("Bye bye dear Server");
			executorService.schedule(new ConnectionRetryRunnable(clientStart), 4, TimeUnit.SECONDS);
		});
		clientStart.launch();
		System.out.println("Launched!");
	}

	private void send() {
		clientStart.send().objectToServer(new TestObject("This should not come back"));
		clientStart.send().registrationToServer(TestObjectTwo.class, new TestObserver());
		try {
			System.out.println("#1 Awaiting receive of Class TestObjectThree...");
			clientStart.send()
					.objectToServer(new Login())
					.andWaitForReceiving(TestObjectThree.class);
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
			System.out.println("Let's com.github.thorbenkuck.netcom2.test the new Connection ..");
			clientStart.send().objectToServer(new TestObject("Hello!"), TestObject.class).andWaitForReceiving(TestObject.class);
			System.out.println("That was good, was'nt it?");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void register() {
//		clientStart.getCommunicationRegistration()
//				.register(TestObject.class)
//				.addLast(o -> System.out.println("Received " + o.getHello() + " from Server"));

		clientStart.getCommunicationRegistration()
				.register(TestObject.class)
				.to(this);

		clientStart.getCommunicationRegistration()
				.register(TestObjectThree.class)
				.addLast(o -> System.out.println("----\n" + o.getMsg() + "\n----"));
	}

	@ReceiveHandler
	public void handle(Connection connection, Session session, TestObject testObject) {
		System.out.println("\n\n\n\n\n\nReceived " + testObject.getHello() + "\n\n\n\n\n\n\n");
	}

	private class TestObserver extends AbstractCacheObserver<TestObjectTwo> {
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

	private class ConnectionRetryRunnable implements Runnable {

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