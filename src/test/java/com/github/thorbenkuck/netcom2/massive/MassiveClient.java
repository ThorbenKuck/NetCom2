package com.github.thorbenkuck.netcom2.massive;

import com.github.thorbenkuck.netcom2.TestObject;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.client.Sender;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MassiveClient implements Runnable {

	private static final int TOTAL_CLIENT_COUNT = 1000;
	private static final CountDownLatch countDownLatch = new CountDownLatch(TOTAL_CLIENT_COUNT * 2);
	private static int receivedCount = 0;
	private static int count = 0;
	private final int MY_COUNT;

	public MassiveClient() {
		synchronized (MassiveClient.class) {
			MY_COUNT = ++count;
		}
	}

	private static synchronized void received(TestObject testObject) {
		++receivedCount;
		countDownLatch.countDown();
	}

	public static void main(String[] args) throws InterruptedException {
		long startTime = System.currentTimeMillis();
		ExecutorService executorService = Executors.newCachedThreadPool();
		for (int i = 0; i < TOTAL_CLIENT_COUNT; i++) {
			executorService.submit(new MassiveClient());
		}

		countDownLatch.await(20, TimeUnit.SECONDS);
		executorService.shutdown();

		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		System.out.println("All Clients took " + elapsedTime + " milliseconds to finish");
		System.out.println("In Seconds: " + TimeUnit.MILLISECONDS.toSeconds(elapsedTime));

		if (receivedCount != TOTAL_CLIENT_COUNT) {
			throw new IllegalStateException("Test was not Successful!\nExpected: " + TOTAL_CLIENT_COUNT + "\nReceived:" + receivedCount);
		}

		System.out.println("\n---\nTEST SUCCESSFUL\n---");
	}

	@Override
	public void run() {
		ClientStart clientStart = ClientStart.at("localhost", 7777);
		clientStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addFirst(MassiveClient::received);
		try {
			clientStart.launch();
			Sender sender = Sender.open(clientStart);
			sender.objectToServer(new TestObject(MY_COUNT + " says hello!"));
		} catch (StartFailedException e) {
			e.printStackTrace();
		}

		System.out.println(Thread.currentThread().toString() + MY_COUNT + " finished. Waiting 3 more seconds ..");

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println(Thread.currentThread().toString() + MY_COUNT + " is now finished");

		countDownLatch.countDown();
	}

}
