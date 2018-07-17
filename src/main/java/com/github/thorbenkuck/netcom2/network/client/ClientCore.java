package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;

import java.net.SocketAddress;
import java.util.function.Supplier;

public interface ClientCore {

	static ClientCore nio() {
		return new NativeNIOClientCore();
	}

	static ClientCore tcp() {
		return new NativeTCPClientCore();
	}

	void blockOnCurrentThread(Supplier<Boolean> running);

	void startBlockerThread(Supplier<Boolean> running);

	void releaseBlocker();

	void establishConnection(SocketAddress socketAddress, Client client) throws StartFailedException;

	void establishConnection(SocketAddress socketAddress, Client client, Class<?> connectionKey) throws StartFailedException;
}
