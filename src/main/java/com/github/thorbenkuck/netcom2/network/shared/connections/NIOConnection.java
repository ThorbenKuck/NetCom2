package com.github.thorbenkuck.netcom2.network.shared.connections;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.pipe.Pipeline;
import com.github.thorbenkuck.netcom2.exceptions.ConnectionDisconnectedException;
import com.github.thorbenkuck.netcom2.exceptions.SendFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.NIOUtils;
import com.github.thorbenkuck.netcom2.network.shared.client.Client;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;

import static com.github.thorbenkuck.netcom2.network.shared.NIOUtils.convertForNIOLog;

class NIOConnection implements Connection {

	private final SocketChannel socketChannel;
	private final Value<Boolean> open = Value.synchronize(false);
	private final Value<Client> clientValue = Value.emptySynchronized();
	private final Value<Class<?>> identifierValue = Value.emptySynchronized();
	private final Logging logging = Logging.unified();
	private final Pipeline<Connection> shutdownPipeline = Pipeline.unifiedCreation();
	private final Queue<RawData> readDataQueue = new LinkedList<>();
	private final Value<Integer> sendBufferSize = Value.synchronize(1024);
	private final Value<Integer> readBufferSize = Value.synchronize(1024);
	private final Value<ByteBuffer> sendBuffer = Value.emptySynchronized();

	NIOConnection(SocketChannel socketChannel) {
		NetCom2Utils.assertNotNull(socketChannel);
		this.socketChannel = socketChannel;
		logging.objectCreated(this);
	}

	@Override
	public void close() throws IOException {
		logging.debug(convertForNIOLog("Closing Connection"));
		logging.trace(convertForNIOLog("This methods only concern is, to shutdown the SocketChannel"));
		logging.trace(convertForNIOLog("Closing underlying SocketChannel"));
		socketChannel.close();
		logging.trace(convertForNIOLog("Applying callback Pipeline"));
		shutdownPipeline.apply(this);
	}

	@Override
	public void open() throws IOException {
		logging.debug(convertForNIOLog("Opening " + this));
		logging.trace(convertForNIOLog("Updating open flag"));
		open.set(true);
		logging.trace(convertForNIOLog("Enforcing socketChannel finish connect"));
		socketChannel.finishConnect();
		logging.trace(convertForNIOLog("Preparing cached SendBuffer (ByteBuffer)"));
		sendBuffer.set(ByteBuffer.allocate(sendBufferSize.get()));
		logging.debug(convertForNIOLog("Connection is now considered open"));
	}

	private void writeTo(ByteBuffer byteBuffer) {
		try {
			logging.trace(convertForNIOLog("Starting to writeTo from ByteBuffer .."));
			while (byteBuffer.hasRemaining()) {
				logging.trace(convertForNIOLog("Found remaining bytes in ByteBuffer .."));
				socketChannel.write(byteBuffer);
			}
			logging.trace(convertForNIOLog("Clearing ByteBuffer .."));
			byteBuffer.clear();
		} catch (IOException e) {
			logging.error(convertForNIOLog("Encountered IOException while writing to SocketChannel!"));
			throw new SendFailedException(e);
		}
	}

	private void writeNewWrapped(byte[] data) {
		logging.debug(convertForNIOLog("Write using new ByteBuffer"));
		logging.trace(convertForNIOLog("Creating ByteBuffer containing " + data.length + " bytes"));
		ByteBuffer byteBuffer = ByteBuffer.wrap(data);
		logging.trace(convertForNIOLog("ByteBuffer value: (size=" + byteBuffer.array().length + ") " + Arrays.toString(byteBuffer.array())));
		logging.trace(convertForNIOLog("Requesting writeTo with newly create ByteBuffer"));
		writeTo(byteBuffer);
	}

	private void writeCached(byte[] data) {
		logging.debug(convertForNIOLog("[a-zA-Z ,().!=:]*"));
		logging.trace(convertForNIOLog("[a-zA-Z ,().!=:]*"));
		synchronized (sendBuffer) {
			if(sendBuffer.isEmpty()) {
				logging.warn(convertForNIOLog("[a-zA-Z ,().!=:]*"));
				logging.trace(convertForNIOLog("[a-zA-Z ,().!=:]*"));
				sendBuffer.set(ByteBuffer.allocate(sendBufferSize.get()));
			}
			ByteBuffer buffer = sendBuffer.get();
			logging.trace(convertForNIOLog("[a-zA-Z ,().!=:]*"));
			logging.trace("Put " + Arrays.toString(data));
			buffer.put(data);
			logging.trace("ByteBuffer value: (size=" + buffer.array().length + ") " + Arrays.toString(buffer.array()));
			buffer.flip();
			logging.trace(convertForNIOLog("[a-zA-Z ,().!=:]*"));
			writeTo(buffer);
		}
	}

	@Override
	public void write(byte[] data) {
		logging.debug(convertForNIOLog("[a-zA-Z ,().!=:]*"));
		logging.trace("Performing sanity-checks on data ..");
		logging.trace(convertForNIOLog("[a-zA-Z ,().!=:]*"));
		if (data.length > sendBufferSize.get()) {
			logging.warn("Cannot use the cached ByteBuffer. The SendBufferSize is set to " + sendBufferSize.get() + " whilst the data-package consists of " + data.length + " entries.");
			logging.trace(convertForNIOLog("[a-zA-Z ,().!=:]*"));
			writeNewWrapped(data);
		} else {
			if (data.length > readBufferSize.get()) {
				logging.warn(convertForNIOLog("[a-zA-Z ,().!=:]*"));
			}

			logging.trace(convertForNIOLog("[a-zA-Z ,().!=:]*"));
			writeCached(data);
		}
	}

	private byte[] readNext(ByteBuffer byteBuffer) throws IOException, ConnectionDisconnectedException {
		logging.debug(convertForNIOLog("[a-zA-Z ,().!=:]*"));
		logging.trace(convertForNIOLog("[a-zA-Z ,().!=:]*"));
		if (!socketChannel.isOpen() || !socketChannel.isConnected()) {
			throw new ConnectionDisconnectedException("SocketChannel is closed");
		}
		int read = socketChannel.read(byteBuffer);
		logging.trace("read " + read);
		if (read < 0) {
			throw new ConnectionDisconnectedException("Disconnect detected");
		}

		byteBuffer.clear();

		return byteBuffer.array();
	}

	@Override
	public void read() throws IOException {
		logging.debug(convertForNIOLog("[a-zA-Z ,().!=:]*"));

		try {
			logging.trace(convertForNIOLog("[a-zA-Z ,().!=:]*"));
			final ByteBuffer byteBuffer = ByteBuffer.allocate(sendBufferSize.get());

			byte[] result = readNext(byteBuffer);

			logging.trace(convertForNIOLog("[a-zA-Z ,().!=:]*"));
			synchronized (readDataQueue) {
				readDataQueue.add(new RawData(result));
			}
		} catch (ConnectionDisconnectedException e) {
			logging.debug(convertForNIOLog("[a-zA-Z ,().!=:]*"));
			close();
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
