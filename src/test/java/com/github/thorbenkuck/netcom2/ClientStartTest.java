package com.github.thorbenkuck.netcom2;

import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.client.Sender;
import com.github.thorbenkuck.netcom2.utility.threaded.NetComThreadPool;

public class ClientStartTest {

	private final ClientStart clientStart;
	private final Sender sender;
	private int id;
	private static int received;

	ClientStartTest() throws StartFailedException {
		System.out.println("Creating ClientStart");
		clientStart = ClientStart.at("localhost", 4444);
		System.out.println("Registering Communication");
		clientStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addFirst(this::print);
		System.out.println("Adding disconnected Handler");
		clientStart.addDisconnectedHandler(client -> System.out.println("Disconnected"));
		System.out.print("Connecting .. ");
		clientStart.launch();
		System.out.println("OK");
		clientStart.startBlockerThread();
		System.out.println("Opening Sender");
		sender = Sender.open(clientStart);
		System.out.print("Logging in .. ");
		sender.objectToServer(new Login());
		System.out.println("OK\n\n\n\nClient ready.\n\n");
	}

	public static void main(String[] args) throws InterruptedException, StartFailedException {
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
			e.printStackTrace(System.out);
			System.exit(1);
		});
		NetComLogging.setLogging(Logging.info());
		NetComThreadPool.startWorkerProcesses(4);
		final int total = 100;
		int checks = 0;
		ClientStartTest clientStartTest = new ClientStartTest();

//		clientStartTest.run();
		while (checks++ < total) {
			try {
				clientStartTest.run();
			} catch (Throwable throwable) {
				throwable.printStackTrace(System.out);
				System.exit(1);
			}
			Thread.sleep(10);
		}
//
		Thread.sleep(3000);
		clientStartTest.stop();
		if (received != total) {
			throw new IllegalStateException("Test was not Successful!\nExpected: " + total + "\nReceived:" + received);
		}

		System.out.println("\n---\nTEST SUCCESSFUL\n---");
	}

	private synchronized int getId() {
		return ++id;
	}

	private void stop() {
		clientStart.softStop();
	}

	private static synchronized void incrementTotalWorkload() {
		++received;
	}

	private void print(TestObject string) {
		System.out.println(Thread.currentThread() + ": " + getId() + " okay");
		incrementTotalWorkload();
	}

	public void run() {
		sender.objectToServer(new TestObject("Hi"));
	}
}