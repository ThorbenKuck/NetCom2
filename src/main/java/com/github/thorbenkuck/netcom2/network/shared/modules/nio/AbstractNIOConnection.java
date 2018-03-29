package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import com.github.thorbenkuck.keller.pipe.Pipeline;
import com.github.thorbenkuck.keller.pipe.PipelineCondition;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.Callback;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public abstract class AbstractNIOConnection implements Connection {

	private final SocketChannel socketChannel;
	private final BlockingQueue<Object> toSend = new LinkedBlockingQueue<>();
	private final Pipeline<Connection> disconnectedPipeline = Pipeline.unifiedCreation();
	private Class<?> key;
	private Logging logging = Logging.unified();

	public AbstractNIOConnection(final SocketChannel socketChannel, final Class<?> key, final Session session) {
		this.socketChannel = socketChannel;
	}

	abstract void beforeSend(Object o);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {

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

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addObjectSendListener(Callback<Object> callback) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addObjectReceivedListener(Callback<Object> callback) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setThreadPool(ExecutorService executorService) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Awaiting startListening() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PipelineCondition<Connection> addOnDisconnectedConsumer(Consumer<Connection> consumer) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OutputStream getOutputStream() throws IOException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BlockingQueue<Object> getSendInterface() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Session getSession() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSession(Session session) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFormattedAddress() {
		return null;
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
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isActive() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getKey() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setKey(Class<?> connectionKey) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLogging(Logging logging) {
		this.logging = logging;
	}
}
