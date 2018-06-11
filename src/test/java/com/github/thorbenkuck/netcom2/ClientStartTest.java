package com.github.thorbenkuck.netcom2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ClientStartTest {

	public ClientStartTest() throws InterruptedException {
		Thread thread = new Thread(() -> {
			final SocketChannel socketChannel;
			try {
				socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 4444));

				byte[] message = "Example String".getBytes();
				ByteBuffer buffer = ByteBuffer.wrap(message);
				socketChannel.write(buffer);

				buffer.clear();

				System.out.println("Writing done");
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		thread.start();

		Thread.sleep(10000);
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		new ClientStartTest();
	}
}