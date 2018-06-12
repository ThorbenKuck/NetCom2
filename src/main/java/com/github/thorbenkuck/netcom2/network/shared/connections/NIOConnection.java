package com.github.thorbenkuck.netcom2.network.shared.connections;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.pipe.Pipeline;
import com.github.thorbenkuck.netcom2.exceptions.SendFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.client.Client;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;

class NIOConnection implements Connection {

	private final SocketChannel socketChannel;
	private final Value<Boolean> open = Value.synchronize(false);
	private final Value<Client> clientValue = Value.emptySynchronized();
	private final Value<Class<?>> identifierValue = Value.emptySynchronized();
	private final Logging logging = Logging.unified();
	private final Pipeline<Connection> shutdownPipeline = Pipeline.unifiedCreation();
	private final Queue<RawData> readDataQueue = new LinkedList<>();

	NIOConnection(SocketChannel socketChannel) {
		NetCom2Utils.assertNotNull(socketChannel);
		this.socketChannel = socketChannel;
	}

	@Override
	public void close() throws IOException {
		socketChannel.close();
		shutdownPipeline.apply(this);
	}

	@Override
	public void open() throws IOException {
		open.set(true);
		socketChannel.finishConnect();
	}

	@Override
	public void write(Object object) {
		logging.debug("Write of " + object + "(" + object.getClass() + ") requested");
		if (object instanceof String) {
			logging.trace("Found String");
			byte[] message = ((String) object).getBytes();
			ByteBuffer buffer = ByteBuffer.wrap(message);
			try {
				socketChannel.write(buffer);
			} catch (IOException e) {
				throw new SendFailedException(e);
			}
		}
		// TODO
	}

	@Override
	public void read() throws IOException {
		logging.debug("Read requested.");
		logging.trace("Allocating ByteBuffer");
		ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
		logging.trace("Reading ..");
		int read = socketChannel.read(byteBuffer);

		logging.trace("read " + read);
		if(read < 0) {
			logging.debug("Connection disconnect detected");
			close();
			return;
		}

		logging.trace("Storing read Data for further processing ");
		synchronized (readDataQueue) {
			readDataQueue.add(new RawData(byteBuffer.array()));
		}
	}

	@Override
	public void hook(Client client) {
		clientValue.set(client);
		client.addConnection(this);
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
	public Optional<Client> hookedClient() {
		return Optional.ofNullable(clientValue.get());
	}

	@Override
	public Optional<SocketAddress> remoteAddress() {
		try {
			return Optional.of(socketChannel.getRemoteAddress());
		} catch (IOException e) {
			logging.catching(e);
			return Optional.empty();
		}
	}

	@Override
	public Optional<SocketAddress> localAddress() {
		try {
			return Optional.of(socketChannel.getLocalAddress());
		} catch (IOException e) {
			logging.catching(e);
			return Optional.empty();
		}
	}

	@Override
	public void addShutdownHook(Consumer<Connection> connectionConsumer) {
		shutdownPipeline.add(connectionConsumer);
	}

	@Override
	public void removeShutdownHook(Consumer<Connection> connectionConsumer) {
		shutdownPipeline.remove(connectionConsumer);
	}

	@Override
	public boolean isOpen() {
		return socketChannel.isOpen();
	}

	@Override
	public Queue<RawData> drain() {
		final Queue<RawData> readDataCopy;
		synchronized (readDataQueue) {
			readDataCopy = new LinkedList<>(readDataQueue);
			readDataQueue.clear();
		}

		return readDataCopy;
	}

	SocketChannel getSocketChannel() {
		return socketChannel;
	}
}
