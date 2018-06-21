package com.github.thorbenkuck.netcom2.network.shared.connections;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.pipe.Pipeline;
import com.github.thorbenkuck.keller.sync.Awaiting;
import com.github.thorbenkuck.keller.sync.Synchronize;
import com.github.thorbenkuck.netcom2.exceptions.ConnectionDisconnectedException;
import com.github.thorbenkuck.netcom2.exceptions.SendFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.function.Consumer;

import static com.github.thorbenkuck.netcom2.network.shared.NIOUtils.convertForNIOLog;

class NIOConnection implements Connection {

	private final SocketChannel socketChannel;
	private final Value<Boolean> open = Value.synchronize(false);
	private final Value<Class<?>> identifierValue = Value.emptySynchronized();
	private final Logging logging = Logging.unified();
	private final Pipeline<Connection> shutdownPipeline = Pipeline.unifiedCreation();
	private final Queue<RawData> readDataQueue = new LinkedList<>();
	private final Value<Integer> sendBufferSize = Value.synchronize(1024);
	private final Value<Integer> readBufferSize = Value.synchronize(1024);
	private final Value<ByteBuffer> sendBuffer = Value.synchronize(ByteBuffer.allocate(readBufferSize.get()));
	private final ConnectionHandler connectionHandler = ConnectionHandler.create();
	private final Synchronize setupSynchronize = Synchronize.createDefault();
	private final Value<Boolean> setupComplete = Value.synchronize(false);
	private final Value<ConnectionContext> connectionContextValue = Value.emptySynchronized();

	NIOConnection(SocketChannel socketChannel) {
		NetCom2Utils.assertNotNull(socketChannel);
		this.socketChannel = socketChannel;
		logging.instantiated(this);
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
		logging.debug(convertForNIOLog("Initializing write using the cached ByteBuffer"));
		logging.trace(convertForNIOLog("Acquiring access over the sendBuffer .."));
		synchronized (sendBuffer) {
			if (sendBuffer.isEmpty()) {
				logging.warn(convertForNIOLog("SendBuffer has been cleared. Do not clear the SendBuffer!"));
				logging.trace(convertForNIOLog("Instantiating new ByteBuffer .."));
				sendBuffer.set(ByteBuffer.allocate(sendBufferSize.get()));
			}
			ByteBuffer buffer = sendBuffer.get();
			logging.trace(convertForNIOLog("Filling ByteBuffer .."));
			buffer.put(data);
			logging.trace(convertForNIOLog("ByteBuffer size=" + buffer.array().length) + ". Flipping buffer ..");
			buffer.flip();
			logging.trace(convertForNIOLog("Requesting write .."));
			writeTo(buffer);
		}
	}

	private byte[] doRead(ByteBuffer byteBuffer) throws IOException, ConnectionDisconnectedException {
		logging.debug(convertForNIOLog("Initializing concrete read from SocketChannel"));
		logging.trace(convertForNIOLog("Checking underlying Socket Channel"));
		if (!socketChannel.isOpen() || !socketChannel.isConnected()) {
			throw new ConnectionDisconnectedException("SocketChannel is closed");
		}
		int read = socketChannel.read(byteBuffer);
		logging.trace("read " + read);

		if (read < 0) {
			logging.debug("Disconnection detected from SocketChannel");
			throw new ConnectionDisconnectedException("Disconnect detected");
		}
		if (read == 0) {
			logging.debug("Found empty read");
			return new byte[0];
		}

		byteBuffer.clear();

		return byteBuffer.array();
	}

	@Override
	public Awaiting connected() {
		return setupSynchronize;
	}

	@Override
	public void close() throws IOException {
		logging.debug(convertForNIOLog("Closing Connection"));
		logging.trace(convertForNIOLog("This methods only concern is, to shutdown the SocketChannel"));
		logging.trace(convertForNIOLog("Closing underlying SocketChannel"));
		socketChannel.close();
		logging.trace(convertForNIOLog("Applying callback Pipeline"));
		shutdownPipeline.apply(this);
		logging.trace(convertForNIOLog("Updating open flag"));
		open.set(false);
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

	@Override
	public void write(String message) {
		write((message + "\r\n").getBytes());
	}

	@Override
	public void write(byte[] data) {
		logging.debug(convertForNIOLog("Starting to write"));
		logging.trace("Performing sanity-checks on data ..");
		logging.trace(convertForNIOLog("checking Data-length to determine th buffer to use"));
		if (data.length > sendBufferSize.get()) {
			logging.warn("Cannot use the cached ByteBuffer. The SendBufferSize is set to " + sendBufferSize.get() + " whilst the data-package consists of " + data.length + " entries.");
			logging.trace(convertForNIOLog("Wrapping data .."));
			writeNewWrapped(data);
		} else {
			logging.trace(convertForNIOLog("Using cached ByteBuffer"));
			writeCached(data);
		}
	}

	@Override
	public void read() throws IOException {
		if (identifierValue.isEmpty()) {
			logging.debug("No ConnectionKey set yet. Stopping read.");
			return;
		}
		logging.debug(convertForNIOLog("Starting to read from " + this));

		try {
			logging.trace(convertForNIOLog("Creating ByteBuffer"));
			final ByteBuffer byteBuffer = ByteBuffer.allocate(sendBufferSize.get());

			logging.trace("Performing read operation");
			byte[] result = doRead(byteBuffer);
			logging.trace("Passing read bytes to the ConnectionHandler, ignoring \\0");
			// Maybe we construct
			// a method for removing
			// \0 bytes without
			// converting to String
			connectionHandler.prepare(new String(result).replaceAll("\0", "").getBytes());

			logging.trace(convertForNIOLog("fetching results from ConnectionHandler .."));
			logging.trace("Fetching read data");
			List<String> read = connectionHandler.takeContents();
			logging.trace("Found " + read.size() + " new received lines");

			read.stream()
					.map(String::getBytes)
					.forEach(bytes -> {
						logging.trace("Passing raw data to readDataQueue");
						synchronized (readDataQueue) {
							readDataQueue.add(new RawData(bytes));
						}
					});

		} catch (final ConnectionDisconnectedException e) {
			logging.debug(convertForNIOLog("Connection close detected!"));
			close();
		}
	}

	@Override
	public void hook(Client client) {
		connectionContextValue.set(ConnectionContext.combine(client, this));
	}

	@Override
	public Optional<Class<?>> getIdentifier() {
		return Optional.ofNullable(identifierValue.get());
	}

	@Override
	public void setIdentifier(Class<?> identifier) {
		logging.debug("Updating identifier to: " + identifier);
		identifierValue.set(identifier);
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
		return open.get() && socketChannel.isOpen();
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

	@Override
	public void finishConnect() {
		logging.debug("Finishing the setup of " + this);
		logging.trace("Releasing waiting Threads ..");
		setupSynchronize.goOn();
		logging.trace("Updating associated flag ..");
		setupComplete.set(true);
		logging.trace("Finishing SocketChannel connect");
		try {
			socketChannel.finishConnect();
		} catch (IOException e) {
			logging.error("Encountered IOException while trying to finish the SocketChannel connect!", e);
		}
		logging.trace("Checking open flag");
		if (!isOpen()) {
			try {
				logging.trace("Connection is not yet opened. Opening");
				open();
				logging.trace("Opened successfully");
			} catch (IOException e) {
				logging.error("Encountered IOException while trying to open the SocketChannel connect!", e);
			}
		}
		logging.info(this + " is now usable!");
	}

	@Override
	public boolean isConnected() {
		return setupComplete.get() && isOpen();
	}

	@Override
	public boolean inSetup() {
		return !setupComplete.get();
	}

	@Override
	public ConnectionContext context() {
		return connectionContextValue.get();
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder("NIOConnection{");
		Optional<SocketAddress> remoteAddress = remoteAddress();
		if (remoteAddress.isPresent()) {
			stringBuilder.append("address=").append(remoteAddress.get());
		} else {
			stringBuilder.append("NOT_CONNECTED");
		}

		stringBuilder.append(", open=").append(open.get());
		stringBuilder.append(", inSetup=").append(inSetup());
		stringBuilder.append(", isConnected=").append(isConnected());
		stringBuilder.append(", identifier=").append(identifierValue.get());

		return stringBuilder.toString();
	}

	SocketChannel getSocketChannel() {
		return socketChannel;
	}
}
