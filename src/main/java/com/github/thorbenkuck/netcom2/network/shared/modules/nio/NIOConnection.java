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
import java.util.Arrays;
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
	private Session session;
	private Class<?> key;
	private Logging logging = Logging.unified();

	public NIOConnection(final SocketChannel socketChannel, Selector selector, final Class<?> key, final Session session, final ObjectHandler objectHandler) {
		this.socketChannel = socketChannel;
		this.selector = selector;
		this.objectHandler = objectHandler;
		this.key = key;
		this.session = session;
	}

	ObjectHandler getObjectHandler() {
		return objectHandler;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {
		socketChannel.keyFor(selector).cancel();
		socketChannel.close();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setup() {
		running.set(true);
//		NetCom2Utils.runOnNetComThread(() -> {
//			while(running.get()) {
//				Object object;
//				try {
//					object = toSend.take();
//					if(object == null) {
//						continue;
//					}
//					write(object);
//				} catch (InterruptedException e) {
//					logging.catching(e);
//				}
//			}
//		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeOnDisconnectedConsumer(Consumer<Connection> consumer) {
		disconnectedPipeline.remove(consumer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(Object object) {
		NetCom2Utils.parameterNotNull(object);
		if (!isActive()) {
			throw new IllegalStateException("Connection is not active");
		}
		String toSend;
		try {
			logging.debug("[NIO]: Send of " + object + " initialized.");
			logging.trace("[NIO]: Serializing ...");
			toSend = objectHandler.serialize(object);
		} catch (SerializationFailedException e) {
			logging.catching(e);
			return;
		}
		logging.trace("[NIO]: Buffering serialized Object ...");
		byte[] message = toSend.getBytes();
		logging.trace("[NIO]: Byte series: {" + message.length + "}" + Arrays.toString(message));
		ByteBuffer buffer = ByteBuffer.wrap(message);
		try {
			logging.trace("[NIO]: Writing buffer to SocketChannel ...");
			int wroteBytes = socketChannel.write(buffer);
			logging.trace("[NIO]: Wrote " + wroteBytes + " bytes");
		} catch (IOException e) {
			if (isActive()) {
				logging.catching(e);
				try {
					close();
				} catch (IOException e1) {
					logging.catching(e1);
				}
			}
			return;
		}
		logging.debug("[NIO]: " + object + " send");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addObjectSendListener(Callback<Object> callback) {
		// TODO
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addObjectReceivedListener(Callback<Object> callback) {
		// TODO
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setThreadPool(ExecutorService executorService) {
		throw new UnsupportedOperationException("NIOConnection#setThreadPool");
	}

	/**
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
	public PipelineCondition<Connection> addOnDisconnectedConsumer(Consumer<Connection> consumer) {
		return disconnectedPipeline.addFirst(consumer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		return socketChannel.socket().getInputStream();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OutputStream getOutputStream() throws IOException {
		return socketChannel.socket().getOutputStream();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BlockingQueue<Object> getSendInterface() {
		return toSend;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Session getSession() {
		return session;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSession(Session session) {
		this.session = session;
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
		return key;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setKey(Class<?> connectionKey) {
		this.key = connectionKey;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLogging(Logging logging) {
		this.logging = logging;
	}
}
