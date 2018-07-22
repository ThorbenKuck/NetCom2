package com.github.thorbenkuck.netcom2.network.shared.connections;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.pipe.Pipeline;
import com.github.thorbenkuck.keller.sync.Awaiting;
import com.github.thorbenkuck.keller.sync.Synchronize;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;

class UDPConnection implements Connection {

	private final DatagramSocket datagramSocket;
	private final Synchronize connectedSynchronize = Synchronize.createDefault();
	private final Logging logging = Logging.unified();
	private final Value<ConnectionContext> contextValue = Value.emptySynchronized();
	private final Value<Class<?>> identifierValue = Value.emptySynchronized();
	private final UDPConnection.ReadingWorker readingWorker;
	private final Queue<RawData> received = new LinkedList<>();
	private final Value<Boolean> inSetup = Value.synchronize(true);
	private final Value<Consumer<Queue<RawData>>> callbackValue = Value.emptySynchronized();
	private final Pipeline<Connection> shutdownPipeline = Pipeline.unifiedCreation();
	private final Value<Boolean> reading = Value.synchronize(false);

	UDPConnection(DatagramSocket datagramSocket) {
		this.datagramSocket = datagramSocket;
		readingWorker = new ReadingWorker(datagramSocket, this::addRawData);
	}

	private void addRawData(RawData readData) {
		synchronized (received) {
			received.add(readData);
			// Still synchronize. This
			// works, because of Javas
			// reentrant synchronize.
			// It ensures, that only one
			// Thread at a time, can
			// inform the EventLoop
			if (!callbackValue.isEmpty()) {
				callbackValue.get().accept(drain());
			}
		}
	}


	@Override
	public Awaiting connected() {
		return null;
	}

	@Override
	public void close() throws IOException {

	}

	@Override
	public void open() throws IOException {

	}

	@Override
	public void write(String message) {

	}

	@Override
	public void write(byte[] data) {

	}

	@Override
	public void read(Consumer<Queue<RawData>> callback) throws IOException {

	}

	@Override
	public void hook(Client client) {

	}

	@Override
	public void read() throws IOException {

	}

	@Override
	public Optional<Class<?>> getIdentifier() {
		return Optional.empty();
	}

	@Override
	public void setIdentifier(Class<?> identifier) {

	}

	@Override
	public Optional<SocketAddress> remoteAddress() {
		return Optional.empty();
	}

	@Override
	public Optional<SocketAddress> localAddress() {
		return Optional.empty();
	}

	@Override
	public void addShutdownHook(Consumer<Connection> connectionConsumer) {

	}

	@Override
	public void removeShutdownHook(Consumer<Connection> connectionConsumer) {

	}

	@Override
	public boolean isOpen() {
		return false;
	}

	@Override
	public Queue<RawData> drain() {
		return null;
	}

	@Override
	public void finishConnect() {

	}

	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	public boolean inSetup() {
		return false;
	}

	@Override
	public ConnectionContext context() {
		return null;
	}

	class ReadingWorker implements Runnable {

		private final DatagramSocket datagramSocket;
		private final Consumer<RawData> receivedCallback;
		private final Value<Boolean> runningValue = Value.of(false);
		private final byte[] buffer = new byte[1024];

		ReadingWorker(DatagramSocket datagramSocket, Consumer<RawData> receivedCallback) {
			this.datagramSocket = datagramSocket;
			this.receivedCallback = receivedCallback;
		}

		private void stop() {
			runningValue.set(false);
		}

		@Override
		public void run() {
			runningValue.set(true);
			while (runningValue.get()) {
				try {
					DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
					datagramSocket.receive(datagramPacket);

					byte[] readData = datagramPacket.getData();
					receivedCallback.accept(new RawData(readData));
				} catch (IOException e) {
					if (!runningValue.get()) {
						logging.catching(e);
						runningValue.set(false);
					}
				}
			}
			runningValue.set(false);
			// disconnect
			try {
				close();
			} catch (IOException e) {
				logging.catching(e);
			}
		}
	}
}
