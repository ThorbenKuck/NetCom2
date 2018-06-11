package com.github.thorbenkuck.netcom2.network.shared;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.function.Consumer;

public interface SelectorChannel {

	static SelectorChannel open() throws IOException {
		return open(Selector.open());
	}

	static SelectorChannel open(Selector selector) {
		return new NativeSelectorChannel(selector);
	}

	void registerForReading(SocketChannel socketChannel);

	void registerForConnection(SocketChannel socketChannel);

	void registerForWrite(SocketChannel socketChannel);

	void registerForAccept(SocketChannel socketChannel);

	void unregister(SocketChannel socketChannel);

	void register(Consumer<SelectionKey> callback, int op);

	void close() throws IOException;

	boolean isRunning();

	void start();

	void wakeup();

	Selector selector();

}
