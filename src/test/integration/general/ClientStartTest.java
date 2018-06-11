package com.github.thorbenkuck.netcom2.integration.general;

import com.github.thorbenkuck.netcom2.annotations.ReceiveHandler;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.integration.*;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.cache.AbstractCacheObserver;
import com.github.thorbenkuck.netcom2.network.shared.cache.CacheObservable;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.modules.Module;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientStartTest {

	private ClientStart clientStart;
	private Logging logging = Logging.unified();
	private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
	private int port = 44444;

	public static void main(String[] args) {
		NetComLogging.setLogging(Logging.warn());
		new ClientStartTest().run();
	}

	private void create() {
		clientStart = ClientStart.at("localhost", port);
	}

	private void setup() {
//		clientStart.setSocketFactory((port, address) -> SSLSocketFactory.getDefault().createSocket(address, port));
		register();
	}

	private void start() throws StartFailedException {
		Module.nio(clientStart);
		clientStart.addFallBackDeSerialization(new TestDeSerializer());
		clientStart.addFallBackSerialization(new TestSerializer());
		clientStart.addDisconnectedHandler(client -> {
			System.out.println("Bye bye dear Server");
			executorService.schedule(new ConnectionRetryRunnable(clientStart), 6, TimeUnit.SECONDS);
		});
		clientStart.launch();
		System.out.println("Launched!");
	}

	private void send() {
		clientStart.send().objectToServer(new TestObject("This should not come back"));
		clientStart.send().registrationToServer(TestObjectTwo.class, new TestObserver());
		try {
			System.out.println("#1 Awaiting receive of Class TestObjectThree after Login request...");
			clientStart.send()
					.objectToServer(new Login());
			Thread.sleep(100);
			System.out.println("#2 Send multiple Logins and TestObject");
			clientStart.send().objectToServer(new Login());
			System.out.println("Send login 1");
			Thread.sleep(100);
			clientStart.send().objectToServer(new Login());
			System.out.println("Send login 2");
			Thread.sleep(100);
			clientStart.send().objectToServer(new Login());
			System.out.println("Send login 3");
			Thread.sleep(100);
			clientStart.send().objectToServer(new TestObject("THIS SHOULD COME BACK!"));
			System.out.println("#3 Initializing new Connection");
			Thread.sleep(100);
//			Awaiting callBack = clientStart.createNewConnection(TestObject.class);
//			Thread.sleep(100);
//			System.out.println("#4 Emulating Parallel workload");
//			System.out.println("SomeStuff");
//			System.out.println("SomeMoreStuff");
//			System.out.println("Now wait for the new Connection..");
//			callBack.synchronize();
//			System.out.println("#5Connection established! YAY!");
//			System.out.println("Let's test the new Connection ..");
//			clientStart.send()
//					.objectToServer(new TestObject("Hello!"), TestObject.class)
//					.andWaitForReceiving(TestObject.class);
//			System.out.println("#6 Finished Test");
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

		clientStart.getCommunicationRegistration()
				.register(MessageFromServer.class)
				.addFirst(o -> System.out.println("MESSAGE\n----\n" + o.getMessage() + "\n----"));
	}

	private void remoteObjectTest() {
		Test test = clientStart.getRemoteObject(Test.class);
		ReturnTest returnTest = clientStart.getRemoteObject(ReturnTest.class);
		ParameterReturnTest parameterReturnTest = clientStart.getRemoteObject(ParameterReturnTest.class);

		System.out.println("\n\n\n\nReturnTest:\n" + returnTest.getReturnValue() + "\n\n\n\n\n");
		System.out.println("\n\n\n\nParameterReturnTest:\n" + parameterReturnTest.concatAndReturn("a test") + "\n\n\n\n\n");
		test.fire();
	}

	public void run() {
		NetComLogging.setLogging(Logging.trace());
		create();
		setup();

		try {
			start();
			remoteObjectTest();
			send();
		} catch (StartFailedException e) {
			e.printStackTrace();
		}
	}

	@ReceiveHandler
	public void handle(Connection connection, Session session, TestObject testObject) {
		System.out.println("\n\n\n\n\n\n[AUTO-INJECTED] Received " + testObject.getHello() + "\n\n\n\n\n\n\n");
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
			++tries;
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