package com.github.thorbenkuck.netcom2.network.shared.connections;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.pipe.Pipeline;
import com.github.thorbenkuck.keller.sync.Awaiting;
import com.github.thorbenkuck.keller.sync.Synchronize;
import com.github.thorbenkuck.netcom2.exceptions.ConnectionDisconnectedException;
import com.github.thorbenkuck.netcom2.exceptions.SendFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.function.Consumer;

class NIOConnection implements Connection {

	private final SocketChannel socketChannel;
	private final Value<Boolean> open = Value.synchronize(false);
	private final Value<Class<?>> identifierValue = Value.emptySynchronized();
	private final Logging logging = Logging.unified();
	private final Pipeline<Connection> shutdownPipeline = Pipeline.unifiedCreation();
	private final Queue<RawData> readDataQueue = new LinkedList<>();
	private final NIOBuffer buffer = new NIOBuffer();
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
			logging.trace("Starting to write to output from ByteBuffer ..");
			logging.trace("ByteBuffer state: " + Arrays.toString(byteBuffer.array()));
			while (byteBuffer.hasRemaining()) {
				// TODO Extract into Selector if read fails 3 times
				logging.trace("Found remaining bytes in ByteBuffer ..");
				socketChannel.write(byteBuffer);
			}
			logging.trace("Successfully performed write");
		} catch (IOException e) {
			logging.error("Encountered IOException while writing to SocketChannel!");
			SendFailedException sendFailedException = new SendFailedException(e);
			logging.trace("Closing " + this);
			try {
				close();
			} catch (IOException e1) {
				logging.error("Close failed!");
				sendFailedException.addSuppressed(e1);
			}
			throw sendFailedException;
		}
	}

	private void writeNewWrapped(byte[] data) {
		logging.debug("Write using possibly new ByteBuffer");
		logging.trace("Requesting ByteBuffer containing " + data.length + " bytes");
		ByteBuffer byteBuffer = buffer.allocate(data);
		logging.trace("ByteBuffer value: (size=" + byteBuffer.capacity() + ") ");
		logging.trace("Checking ByteBuffer size if it fits the data");
		if (data.length > byteBuffer.capacity()) {
			throw new IllegalStateException("[InternalError]: The requested ByteBuffer has an invalid size. Please submit this to github");
		}
		logging.trace("Requesting writeTo with fetched ByteBuffer");
		writeTo(byteBuffer);
		logging.trace("Freeing used ByteBuffer ");
		buffer.free(byteBuffer);
	}

	private byte[] doRead(final ByteBuffer byteBuffer) throws ConnectionDisconnectedException {
		logging.debug("Initializing concrete read from SocketChannel");
		logging.trace("Checking underlying Socket Channel");
		if (!socketChannel.isOpen() || !socketChannel.isConnected()) {
			throw new ConnectionDisconnectedException("SocketChannel is closed");
		}
		final int read;
		try {
			logging.trace("Performing initial read");
			read = socketChannel.read(byteBuffer);
		} catch (final IOException e) {
			throw new ConnectionDisconnectedException("Connection is not open anymore!", e);
		}
		logging.trace("read " + Arrays.toString(byteBuffer.array()));

		if (read < 0) {
			logging.debug("Disconnection detected from SocketChannel");
			throw new ConnectionDisconnectedException("Disconnect detected");
		}
		if (read == 0) {
			logging.debug("Found empty read");
			return new byte[0];
		}

		byte[] result = byteBuffer.array();

		byteBuffer.clear();

		return result;
	}

	@Override
	public Awaiting connected() {
		return setupSynchronize;
	}

	@Override
	public void close() throws IOException {
		logging.debug("Closing Connection");
		logging.trace("This methods only concern is, to shutdown the SocketChannel");
		logging.trace("Closing underlying SocketChannel");
		socketChannel.close();
		logging.trace("Applying callback Pipeline");
		shutdownPipeline.apply(this);
		logging.trace("Updating open flag");
		open.set(false);
	}

	@Override
	public void open() throws IOException {
		logging.debug("Opening " + this);
		logging.trace("Updating open flag");
		open.set(true);
		logging.trace("Enforcing socketChannel finish connect");
		socketChannel.finishConnect();
		logging.debug("Connection is now considered open");
	}

	@Override
	public void write(String message) {
		write((message + "\r\n").getBytes());
	}

	@Override
	public void write(byte[] data) {
		logging.debug("Starting to write");
		logging.trace("Performing sanity-checks on data ..");
		if (data == null) {
			throw new SendFailedException("Can not send null data");
		}
		logging.trace("Wrapping data ..");
		writeNewWrapped(data);
		logging.debug("Successfully wrote data to Connection");
	}

	@Override
	public void hook(ConnectionContext context) {
		connectionContextValue.set(context);
	}

	@Override
	public void read(Consumer<Queue<RawData>> callback) throws IOException {
		read();
		callback.accept(drain());
	}

	@Override
	public void read() throws IOException {
		if (identifierValue.isEmpty()) {
			logging.warn("No ConnectionKey set yet. Stopping read.");
			return;
		}
		logging.debug("Starting to read from " + this);
		ByteBuffer byteBuffer = null;
		try {
			logging.trace("Creating ByteBuffer");
			byteBuffer = buffer.allocate();
			logging.trace("ByteBuffer size=" + byteBuffer.capacity());

			logging.trace("Performing read operation");
			byte[] result = doRead(byteBuffer);
			logging.trace("Passing read bytes to the ConnectionHandler, ignoring \\0");
			// Maybe we construct
			// a method for removing
			// \0 bytes without
			// converting to String
			connectionHandler.prepare(new String(result).replaceAll("\0", "").getBytes());

			logging.trace("fetching results from ConnectionHandler ..");
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
			logging.debug("Connection close detected!");
			close();
		} finally {
			if (byteBuffer != null) {
				logging.trace("Marking used ByteBuffer as reusable");
				buffer.free(byteBuffer);
			}
		}
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
			return Optional.empty();
		}
	}

	@Override
	public Optional<SocketAddress> localAddress() {
		try {
			return Optional.of(socketChannel.getLocalAddress());
		} catch (IOException e) {
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
