package com.github.thorbenkuck.netcom2;

import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.client.Sender;

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

	public ClientStartTest() throws StartFailedException {
		clientStart = ClientStart.at("localhost", 4444);
		NetComLogging.setLogging(Logging.trace());
		clientStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addFirst(this::print);
		clientStart.launch();
		clientStart.blockOnCurrentThread();
		sender = Sender.open(clientStart);
	}

	public static void main(String[] args) throws InterruptedException, StartFailedException {
		int checks = 0;
		ClientStartTest clientStartTest = new ClientStartTest();
//		while(checks++ < 100) {
//			try {
//				clientStartTest.run(checks);
//			} catch (StartFailedException e) {
//				e.printStackTrace(System.out);
//			}
//			Thread.sleep(10);
//		}
//
//		Thread.sleep(10000);
	}

	private void print(TestObject string) {
		System.out.println(Thread.currentThread() + ": " + id + " okay");
	}

	public void run(int id) throws StartFailedException, InterruptedException {
		this.id = id;
		sender.objectToServer(new TestObject("Hi"));
		sender.objectToServer(new Login());
		sender.objectToServer(new TestObject("Hi"));
		sender.objectToServer(new Logout());
	}
}