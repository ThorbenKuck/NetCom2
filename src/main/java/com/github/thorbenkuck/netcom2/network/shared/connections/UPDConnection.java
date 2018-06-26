package com.github.thorbenkuck.netcom2.network.shared.connections;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.sync.Awaiting;
import com.github.thorbenkuck.keller.sync.Synchronize;
import com.github.thorbenkuck.netcom2.exceptions.SendFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.utility.threaded.NetComThreadPool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;

class UPDConnection implements Connection {

	private final Synchronize connectedSynchronize = Synchronize.createDefault();
	private final Socket socket;
	private final Logging logging = Logging.unified();
	private final Value<ConnectionContext> contextValue = Value.emptySynchronized();
	private final Value<Class<?>> identifierValue = Value.emptySynchronized();
	private final ReadingWorker readingWorker;
	private final Queue<RawData> received = new LinkedList<>();

	UPDConnection(Socket socket) {
		this.socket = socket;
		readingWorker = new ReadingWorker(socket, this::addRawData);
	}

	private void addRawData(RawData readData) {
		synchronized (received) {
			received.add(readData);
		}
	}

	@Override
	public Awaiting connected() {
		return connectedSynchronize;
	}

	@Override
	public void close() throws IOException {
		connectedSynchronize.reset();
		readingWorker.stop();
		socket.close();
	}

	@Override
	public void open() throws IOException {
		socket.setKeepAlive(true);
		NetComThreadPool.submitCustomWorkerTask(readingWorker);
	}

	@Override
	public void write(String message) {
		write(message.getBytes());
	}

	@Override
	public void write(byte[] data) {
		try {
			socket.getOutputStream().write(data);
		} catch (IOException e) {
			throw new SendFailedException(e);
		}
	}

	@Override
	public void read(Consumer<Queue<RawData>> callback) throws IOException {

	}

	@Override
	public void hook(Client client) {
		contextValue.set(ConnectionContext.combine(client, this));
	}

	@Override
	public void read() throws IOException {

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
	public Optional<SocketAddress> remoteAddress() {
		return Optional.ofNullable(socket.getRemoteSocketAddress());
	}

	@Override
	public Optional<SocketAddress> localAddress() {
		return Optional.ofNullable(socket.getLocalSocketAddress());
	}

	@Override
	public void addShutdownHook(Consumer<Connection> connectionConsumer) {

	}

	@Override
	public void removeShutdownHook(Consumer<Connection> connectionConsumer) {

	}

	@Override
	public boolean isOpen() {
		return !socket.isClosed();
	}

	@Override
	public Queue<RawData> drain() {
		Queue<RawData> copy;
		synchronized (received) {
			copy = new LinkedList<>(received);
			received.clear();
		}
		return copy;
	}

	@Override
	public void finishConnect() {

	}

	@Override
	public boolean isConnected() {
		return socket.isConnected();
	}

	@Override
	public boolean inSetup() {
		return false;
	}

	@Override
	public ConnectionContext context() {
		return contextValue.get();
	}

	private final class ReadingWorker implements Runnable {

		private final Consumer<RawData> receivedCallback;
		private final Value<Boolean> runningValue = Value.of(false);
		private final Socket socket;

		private ReadingWorker(Socket socket, Consumer<RawData> receivedCallback) {
			this.receivedCallback = receivedCallback;
			this.socket = socket;
		}

		private void stop() {
			runningValue.set(false);
		}

		@Override
		public void run() {
			runningValue.set(true);
			BufferedReader inputStream;
			try {
				inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			} catch (IOException e) {
				logging.catching(e);
				return;
			}
			while (runningValue.get()) {
				try {
					// TODO read correctly from InputStream
					String read = inputStream.readLine();
					if (read == null) {
						runningValue.set(false);
						continue;
					}

					byte[] readData = (read + "\r\n").getBytes();
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
