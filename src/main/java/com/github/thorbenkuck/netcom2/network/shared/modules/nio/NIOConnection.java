package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.pipe.Pipeline;
import com.github.thorbenkuck.keller.pipe.PipelineCondition;
import com.github.thorbenkuck.netcom2.exceptions.SerializationFailedException;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.Callback;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.Synchronize;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

final class NIOConnection implements Connection {

	private final SocketChannel socketChannel;
	private final Selector selector;
	private final ObjectHandler objectHandler;
	private final BlockingQueue<Object> toSend = new LinkedBlockingQueue<>();
	private final Pipeline<Connection> disconnectedPipeline = Pipeline.unifiedCreation();
	private final Value<Boolean> running = Value.synchronize(false);
	private final Value<Session> session;
	private final Value<Class<?>> key;
	private final List<Callback<Object>> sendCallbacks = new ArrayList<>();
	private final List<Callback<Object>> receiveCallbacks = new ArrayList<>();
	private final Client client;
	private Logging logging = Logging.unified();

	public NIOConnection(final SocketChannel socketChannel, final Selector selector, final Class<?> key, final Session session, final ObjectHandler objectHandler, Client client) {
		this.socketChannel = socketChannel;
		this.selector = selector;
		this.objectHandler = objectHandler;
		this.key = Value.synchronize(key);
		this.session = Value.synchronize(session);
		this.client = client;
	}

	private void callbackSend(final Object object) {
		NetCom2Utils.parameterNotNull(object);
		logging.debug("[NIO] Accepting SendCallbacks(" + object + ")!");
		logging.trace("[NIO] Calling all callbacks, that want to be called ..");
		final List<Callback<Object>> temp;
		synchronized (sendCallbacks) {
			temp = new ArrayList<>(sendCallbacks);
		}

		temp.stream()
				.filter(callBack -> callBack.isAcceptable(object))
				.forEach(callBack -> {
					logging.trace("[NIO] Calling " + callBack + " ..");
					callBack.accept(object);
				});

		logging.trace("[NIO] Requesting to cleanup SendCallbacks");
		cleanUpSendCallbacks();
	}

	/**
	 * Clears up all Callbacks, that are removable
	 */
	private void cleanUpReceiveCallbacks() {
		logging.debug("[NIO] ReceiveCallback cleanup requested!");
		final List<Callback<Object>> removable = new ArrayList<>();
		synchronized (receiveCallbacks) {
			receiveCallbacks.stream()
					.filter(Callback::isRemovable)
					.forEachOrdered(callBack -> {
						logging.debug("[NIO] Marking " + callBack + " as to be removed ..");
						removable.add(callBack);
					});
		}

		removable.forEach(callback -> removeCallback(callback, receiveCallbacks));

		logging.debug("[NIO] ReceiveCallback cleanup done!");
	}

	private void cleanUpSendCallbacks() {
		logging.debug("[NIO] SendCallback cleanup requested!");
		final List<Callback<Object>> toRemove = new ArrayList<>();
		synchronized (sendCallbacks) {
			sendCallbacks.stream()
					.filter(Callback::isRemovable)
					.forEach(callBack -> {
						logging.trace("[NIO] Marking Callback " + callBack + " as to be removed ..");
						toRemove.add(callBack);
					});
		}

		toRemove.forEach(callback -> removeCallback(callback, sendCallbacks));

		logging.debug("[NIO] SendCallback cleanup done!");
	}

	private void removeCallback(final Callback<Object> toRemove, Collection<Callback<Object>> root) {
		NetCom2Utils.parameterNotNull(toRemove, root);
		logging.trace("[NIO] Preparing to remove Callback: " + toRemove);
		toRemove.onRemove();
		logging.debug("[NIO] Removing Callback " + toRemove);
		root.remove(toRemove);
	}

	final ObjectHandler getObjectHandler() {
		return objectHandler;
	}

	/**
	 * Triggers all Callbacks, listening for sending Objects.
	 * <p>
	 * Afterwards, it will clean up to free up resources.
	 *
	 * @param object the Object that was send.
	 */
	final void callbackReceived(final Object object) {
		NetCom2Utils.parameterNotNull(object);
		logging.debug("[NIO] Accepting ReceivedCallbacks(" + object + ")!");
		logging.trace("[NIO] Calling all callbacks, that want to be called ..");
		final List<Callback<Object>> temp;
		synchronized (receiveCallbacks) {
			temp = new ArrayList<>(receiveCallbacks);
		}
		temp.stream()
				.filter(callBack -> callBack.isAcceptable(object))
				.forEachOrdered(callBack -> {
					logging.trace("[NIO] Calling " + callBack + " ..");
					callBack.accept(object);
				});

		logging.trace("[NIO] Requesting to cleanup ReceiveCallbacks");
		cleanUpReceiveCallbacks();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {
		logging.info("[NIO] Closing this Connection!");
		logging.trace("[NIO] Canceling selector keys ..");
		socketChannel.keyFor(selector).cancel();
		logging.trace("[NIO] Closing SocketChannel ..");
		socketChannel.close();
		logging.trace("[NIO] Running disconnectedPipeline ..");
		disconnectedPipeline.apply(this);
		logging.trace("[NIO] Setting running flag to false ..");
		running.set(false);
		logging.trace("[NIO] Removing from Client ..");
		client.removeConnection(key.get());
		if (client.countConnections() == 0) {
			logging.debug("[NIO] No Connections left .. Disconnect of client initialized.");
			client.disconnect();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setup() {
		running.set(true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeOnDisconnectedConsumer(final Consumer<Connection> consumer) {
		NetCom2Utils.parameterNotNull(consumer);
		disconnectedPipeline.remove(consumer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(final Object object) {
		NetCom2Utils.parameterNotNull(object);
		if (!isActive()) {
			throw new IllegalStateException("Connection is not active");
		}
		final String toSend;
		try {
			logging.info("[NIO] Send of " + object + " initialized.");
			logging.trace("[NIO] Serializing ...");
			toSend = objectHandler.serialize(object);
		} catch (final SerializationFailedException e) {
			logging.catching(e);
			return;
		}
		logging.trace("[NIO] Buffering serialized Object ...");
		final byte[] message = toSend.getBytes();
		logging.trace("[NIO] Byte series: {" + message.length + "}" + Arrays.toString(message));
		final ByteBuffer buffer = ByteBuffer.wrap(message);
		try {
			logging.trace("[NIO] Writing buffer to SocketChannel ...");
			final int wroteBytes = socketChannel.write(buffer);
			logging.trace("[NIO] Wrote " + wroteBytes + " bytes");
			callbackSend(object);
		} catch (final IOException e) {
			if (isActive()) {
				logging.catching(e);
				try {
					close();
				} catch (final IOException e1) {
					logging.catching(e1);
				}
			}
			return;
		}
		logging.debug("[NIO] " + object + " send");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addObjectSendListener(final Callback<Object> callback) {
		NetCom2Utils.parameterNotNull(callback);
		synchronized (sendCallbacks) {
			sendCallbacks.add(callback);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addObjectReceivedListener(final Callback<Object> callback) {
		NetCom2Utils.parameterNotNull(callback);
		synchronized (receiveCallbacks) {
			receiveCallbacks.add(callback);
		}
	}

	/**
	 * In NIO, there is no need for an ExecutorService within the NIOConnection.
	 * <p>
	 * {@inheritDoc}
	 *
	 * @throws UnsupportedOperationException this operation is not supported with NIO
	 */
	@Override
	public void setThreadPool(final ExecutorService executorService) {
		throw new UnsupportedOperationException("NIOConnection#setThreadPool");
	}

	/**
	 * The NIOConnection is created, only if the physical Connection already is established.
	 * <p>
	 * This means, the Connection is always listening.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public Awaiting startListening() {
		return Synchronize.empty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PipelineCondition<Connection> addOnDisconnectedConsumer(final Consumer<Connection> consumer) {
		NetCom2Utils.parameterNotNull(consumer);
		return disconnectedPipeline.addFirst(consumer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		logging.warn("[NIO] In nonblocking IO, this Method will invoke an IllegalBlockingModeException!");
		return socketChannel.socket().getInputStream();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OutputStream getOutputStream() throws IOException {
		logging.warn("[NIO] In nonblocking IO, this Method will invoke an IllegalBlockingModeException!");
		return socketChannel.socket().getOutputStream();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BlockingQueue<Object> getSendInterface() {
		logging.warn("[NIO] The SendInterface will have no effect in nonblocking IO!");
		return toSend;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Session getSession() {
		return session.get();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSession(final Session session) {
		NetCom2Utils.parameterNotNull(session);
		this.session.set(session);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFormattedAddress() {
		return getInetAddress().toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getPort() {
		return socketChannel.socket().getPort();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InetAddress getInetAddress() {
		try {
			return ((InetSocketAddress) socketChannel.getRemoteAddress()).getAddress();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isActive() {
		return socketChannel.isConnected() && socketChannel.isOpen();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getKey() {
		return key.get();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setKey(final Class<?> connectionKey) {
		NetCom2Utils.parameterNotNull(connectionKey);
		this.key.set(connectionKey);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLogging(final Logging logging) {
		NetCom2Utils.parameterNotNull(logging);
		this.logging = logging;
	}
}
