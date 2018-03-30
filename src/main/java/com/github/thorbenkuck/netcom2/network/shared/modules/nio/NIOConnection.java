package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import com.github.thorbenkuck.keller.pipe.Pipeline;
import com.github.thorbenkuck.keller.pipe.PipelineCondition;
import com.github.thorbenkuck.netcom2.exceptions.SendFailedException;
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
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class NIOConnection implements Connection {

	private final SocketChannel socketChannel;
	private final ObjectHandler objectHandler;
	private final BlockingQueue<Object> toSend = new LinkedBlockingQueue<>();
	private final Pipeline<Connection> disconnectedPipeline = Pipeline.unifiedCreation();
	private Session session;
	private Class<?> key;
	private Logging logging = Logging.unified();

	public NIOConnection(final SocketChannel socketChannel, final Class<?> key, final Session session, final ObjectHandler objectHandler) {
		this.socketChannel = socketChannel;
		this.objectHandler = objectHandler;
		this.key = key;
		this.session = session;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {
		socketChannel.close();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setup() {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeOnDisconnectedConsumer(Consumer<Connection> consumer) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(Object object) {
		NetCom2Utils.runOnNetComThread(() -> {
			String toSend;
			try {
				logging.debug("Send of " + object + " initialized.");
				logging.trace("Serializing ...");
				toSend = objectHandler.serialize(object);
			} catch (SerializationFailedException e) {
				throw new SendFailedException(e);
			}
			logging.trace("Buffering serialized Object ...");
			ByteBuffer buffer = ByteBuffer.wrap(toSend.getBytes());
			try {
				logging.trace("Writing buffer to SocketChannel ...");
				socketChannel.write(buffer);
			} catch (IOException e) {
				throw new SendFailedException(e);
			}
			logging.debug(object + " send");
		});
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
		throw new UnsupportedOperationException("TO BE REMOVED!");
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
		return new LinkedBlockingQueue<>();
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
		return 0;
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
		return true;
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
