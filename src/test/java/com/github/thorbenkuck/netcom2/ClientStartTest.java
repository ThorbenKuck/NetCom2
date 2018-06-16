package com.github.thorbenkuck.netcom2;

import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.client.Sender;

import java.io.IOException;

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

	public ClientStartTest() {
		clientStart = ClientStart.at("localhost", 4444);
	}

	public static void main(String[] args) throws InterruptedException {
		int checks = 0;
//		while(checks++ < 1000) {
			try {
				new ClientStartTest().run();
			} catch (StartFailedException e) {
				e.printStackTrace(System.out);
			}
		Thread.sleep(1000);

			System.out.println();
			System.out.println(new String(new byte[]{127}));
//		}
	}

	private void print(String string) {
		synchronized (System.out) {
			System.out.println("[" + Thread.currentThread() + "]: " + string);
		}
	}

	public void run() throws StartFailedException {
		NetComLogging.setLogging(Logging.trace());
		clientStart.getCommunicationRegistration()
				.register(String.class)
				.addFirst(this::print);
		clientStart.launch();
		Sender sender = Sender.open(clientStart);
		for(int i = 0 ; i < 100 ; i++) {
			sender.objectToServer("Hi\n\r");
		}
	}
}