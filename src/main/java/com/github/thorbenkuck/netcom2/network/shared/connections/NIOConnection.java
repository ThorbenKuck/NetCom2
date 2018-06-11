package com.github.thorbenkuck.netcom2.network.shared.connections;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.client.Client;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Optional;

class NIOConnection implements Connection {

	private final SocketChannel socketChannel;
	private final Value<Boolean> open = Value.synchronize(false);
	private final Value<Client> clientValue = Value.emptySynchronized();
	private final Value<Class<?>> identifierValue = Value.emptySynchronized();
	private final Logging logging = Logging.unified();

	NIOConnection(SocketChannel socketChannel) {
		NetCom2Utils.assertNotNull(socketChannel);
		this.socketChannel = socketChannel;
	}

	@Override
	public void close() throws IOException {
		socketChannel.close();
	}

	@Override
	public void open() throws IOException {
		open.set(true);
		socketChannel.finishConnect();
	}

	@Override
	public void write(Object object) {
		// TODO
	}

	@Override
	public void read() {
		// TODO
	}

	@Override
	public void hook(Client client) {
		clientValue.set(client);
	}

	@Override
	public Optional<Class<?>> getIdentifier() {
		return Optional.ofNullable(identifierValue.get());
	}

	@Override
	public void setIdentifier(Class<?> identifier) {
		identifierValue.set(identifier);
	}

	@Override
	public Optional<Client> getHookedClient() {
		return Optional.ofNullable(clientValue.get());
	}

	@Override
	public Optional<SocketAddress> getRemoteAddress() {
		try {
			return Optional.of(socketChannel.getRemoteAddress());
		} catch (IOException e) {
			logging.catching(e);
			return Optional.empty();
		}
	}

	@Override
	public Optional<SocketAddress> getLocalAddress() {
		try {
			return Optional.of(socketChannel.getLocalAddress());
		} catch (IOException e) {
			logging.catching(e);
			return Optional.empty();
		}
	}

	SocketChannel getSocketChannel() {
		return socketChannel;
	}
}
