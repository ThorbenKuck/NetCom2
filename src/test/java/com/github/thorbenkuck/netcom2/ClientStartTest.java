package com.github.thorbenkuck.netcom2;

import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.client.Sender;
import com.github.thorbenkuck.netcom2.utility.threaded.NetComThreadPool;

public class ClientStartTest {

//	public ClientStartTest() throws InterruptedException {
//		Thread thread = new Thread(() -> {
//			final SocketChannel socketChannel;
//			try {
//				socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 4444));
//
//				byte[] message = "Example String".getBytes();
//				ByteBuffer buffer = ByteBuffer.wrap(message);
//				socketChannel.write(buffer);
//
//				buffer.clear();
//
//				System.out.println("Writing done");
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		});
//
//		thread.start();
//
//		Thread.sleep(10000);
//	}

	private final ClientStart clientStart;
	private final Sender sender;
	private int id;

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
		NetComThreadPool.startWorkerTask();
		NetComThreadPool.startWorkerTask();
		NetComThreadPool.startWorkerTask();
		NetComThreadPool.startWorkerTask();
		int checks = 0;
		ClientStartTest clientStartTest = new ClientStartTest();
		while (checks++ < 100) {
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
	}

	private synchronized int getId() {
		return ++id;
	}

	private void stop() {
		clientStart.softStop();
	}

	private void print(TestObject string) {
		System.out.println(Thread.currentThread() + ": " + getId() + " okay");
	}

	public void run() {
		sender.objectToServer(new TestObject("Hi"));
	}
}