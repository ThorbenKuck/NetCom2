package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.keller.pipe.Pipeline;
import com.github.thorbenkuck.keller.pipe.PipelineCondition;
import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.annotations.Experimental;
import com.github.thorbenkuck.netcom2.exceptions.ClientCreationFailedException;
import com.github.thorbenkuck.netcom2.interfaces.Mutex;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.interfaces.ReceivingService;
import com.github.thorbenkuck.netcom2.network.interfaces.SendingService;
import com.github.thorbenkuck.netcom2.network.shared.Callback;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.Synchronize;
import com.github.thorbenkuck.netcom2.network.synchronization.DefaultSynchronize;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

public abstract class AbstractConnection implements Connection, Mutex {

	private final Socket socket;
	private final BlockingQueue<Object> toSend = new LinkedBlockingQueue<>();
	private final Pipeline<Connection> disconnectedPipeline = Pipeline.unifiedCreation();
	private final Semaphore semaphore = new Semaphore(1);
	private boolean setup;
	private boolean started;
	private Session session;
	private Class<?> key;
	private ExecutorService threadPool = NetCom2Utils.createNewCachedExecutorService();
	protected Logging logging = Logging.unified();
	protected SendingService sendingService;
	protected ReceivingService receivingService;

	protected AbstractConnection(final Socket socket, final SendingService sendingService,
								 final ReceivingService receivingService,
								 final Session session, final Class<?> key) {
		this.socket = socket;
		this.sendingService = sendingService;
		this.receivingService = receivingService;
		this.session = session;
		this.key = key;
	}

	/**
	 * This method is called, before an Object is beforeSend to the client.
	 *
	 * @param o the Object
	 */
	protected abstract void beforeSend(final Object o);

	/**
	 * This method is called if an object is received and after its Communication is triggered
	 *
	 * @param o the Object
	 */
	abstract void receivedObject(final Object o);

	/**
	 * This method is only called, if the Connection is closed.
	 * <p>
	 * To be exact, it will be called AFTER the closing routing
	 */
	protected abstract void onClose();

	/**
	 * This method is called, after an Object has been send
	 *
	 * @param o the Object that just was send.
	 */
	protected abstract void afterSend(final Object o);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLogging(final Logging logging) {
		this.logging.debug("Overriding set Logging ..");
		this.logging = logging;
		logging.debug("Overrode Logging!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {
		try {
			logging.debug("Closing Connection " + this);
			logging.trace("Requesting soft-stop of set ReceivingService ..");
			receivingService.softStop();
			logging.trace("Requesting soft-stop of set SendingService ..");
			sendingService.softStop();
			logging.trace("Shutting down socket ..");
			socket.close();
			logging.debug("Successfully shut down Connection " + this);
		} finally {
			onClose();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setup() {
		logging.debug("Connection setup for " + socket);
		try {
			logging.trace("SendingService setup..");
			synchronized (this) {
				this.sendingService.setup(socket.getOutputStream(), toSend);
				logging.trace("SendingService was successfully setup!");
				logging.trace("ReceivingService setup..");
				this.receivingService.setup(this, getSession());
			}
			logging.trace("ReceivingService was successfully setup!");
			logging.trace("Adding Call-Back-Hook to ReceivingService");
			receivingService.addReceivingCallback(new DefaultReceiveCallback());
		} catch (IOException e) {
			try {
				logging.warn("Encountered Exception while ConnectionSetup!");
				logging.catching(e);
				close();
			} catch (IOException e1) {
				e.addSuppressed(e1);
				logging.fatal("Encountered Exception while cleaning up over a previously encountered Exception!", e1);
			}
			throw new ClientCreationFailedException(e);
		}
		setup = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeOnDisconnectedConsumer(final Consumer<Connection> consumer) {
		NetCom2Utils.parameterNotNull(consumer);
		logging.debug("Removed DisconnectedConsumer(" + consumer + ") from Connection " + this);
		disconnectedPipeline.remove(consumer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
	@Override
	public void write(final Object object) {
		NetCom2Utils.parameterNotNull(object);
		if (! setup) {
			throw new IllegalStateException("Connection has to be setup to beforeSend objects!");
		}
		if (! isActive()) {
			throw new IllegalStateException("Connection is closed");
		}
		logging.trace("Running write in new Thread to write " + object + " ..");
		threadPool.submit(() -> {
			logging.trace("notifying of new Object to send ..");
			beforeSend(object);
			logging.trace("Offering object " + object + " to write..");
			toSend.offer(object);
			logging.trace("notifying of new Object extracted of thread ..");
			afterSend(object);
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addObjectSendListener(final Callback<Object> callback) {
		NetCom2Utils.parameterNotNull(callback);
		logging.trace("Adding SendCallback " + callback + " to " + this);
		sendingService.addSendDoneCallback(callback);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addObjectReceivedListener(final Callback<Object> callback) {
		NetCom2Utils.parameterNotNull(callback);
		logging.trace("Adding ReceiveCallback " + callback + " to " + this);
		receivingService.addReceivingCallback(callback);
	}

	/**
	 * TODO Find a functioning way. Should be achievable.. I hope..
	 * {@inheritDoc}
	 */
	@Experimental
	@Override
	public void setThreadPool(final ExecutorService executorService) {
		logging.error("This operation is not yet supported!");
		/*
		// Soft-Stop currentThreadPool
		// set new ThreadPool
		// Restart Sending and ReceivingService
		// Catch up with pending messages

		// Since this is not working, it is
		// commented out. It should be possible
		// tho. Check for NetComThread stuff
		// Maybe this will help to "migrate" runnable.
		sendingService.softStop();
		receivingService.softStop();
		threadPool.shutdown();
		this.threadPool = executorService;
		try {
			startListening().synchronize();
		} catch (InterruptedException e) {
			logging.catching(e);
		}
		*/
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Synchronize startListening() {
		if (! setup) {
			throw new IllegalStateException("Connection has to be setup to listen!");
		}
		if (started) {
			throw new IllegalStateException("Cannot startListening! Connection is already listening ");
		}

		final Synchronize synchronize = new DefaultSynchronize();
		logging.debug("Starting to listen to: " + this);
		threadPool.submit(() -> {
			try {
				logging.trace("Awaiting Synchronization of ReceivingService");
				receivingService.onDisconnect(() -> disconnectedPipeline.run(this));
				receivingService.started().synchronize();
				logging.trace("Awaiting Synchronization of SendingService");
				sendingService.setConnectionIDSupplier(this::toString);
				sendingService.started().synchronize();
			} catch (final InterruptedException e) {
				logging.catching(e);
			}
			logging.info("Synchronization complete! Connection is now listening.");
			started = true;
			logging.trace("Releasing awaiting Threads..");
			synchronize.goOn();
		});
		logging.trace("Executing ReceivingService ..");
		threadPool.submit(receivingService);
		logging.trace("Executing SendingService ..");
		threadPool.submit(sendingService);

		return synchronize;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PipelineCondition<Connection> addOnDisconnectedConsumer(final Consumer<Connection> consumer) {
		NetCom2Utils.parameterNotNull(consumer);
		logging.debug("Added DisconnectedConsumer(" + consumer + ") for Connection " + this);
		return disconnectedPipeline.addLast(consumer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final InputStream getInputStream() throws IOException {
		return socket.getInputStream();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final OutputStream getOutputStream() throws IOException {
		return socket.getOutputStream();
	}

	@Override
	public BlockingQueue<Object> getSendInterface() {
		return toSend;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Session getSession() {
		return session;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setSession(final Session session) {
		NetCom2Utils.parameterNotNull(session);
		logging.debug("Overriding Session for " + this);
		receivingService.setSession(session);
		this.session = session;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFormattedAddress() {
		return getInetAddress() + ":" + getPort();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getPort() {
		return socket.getPort();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InetAddress getInetAddress() {
		return socket.getInetAddress();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isActive() {
		return socket.isConnected() && sendingService.running() && receivingService.running();
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
	public void setKey(final Class<?> connectionKey) {
		NetCom2Utils.parameterNotNull(connectionKey);
		this.key = connectionKey;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (! (o instanceof AbstractConnection)) return false;

		final AbstractConnection that = (AbstractConnection) o;

		return setup == that.setup && started == that.started && socket.equals(that.socket)
				&& toSend.equals(that.toSend) && disconnectedPipeline.equals(that.disconnectedPipeline)
				&& semaphore.equals(that.semaphore) && session.equals(that.session)
				&& key.equals(that.key) && threadPool.equals(that.threadPool)
				&& sendingService.equals(that.sendingService) && receivingService.equals(that.receivingService);
	}

	@Override
	public int hashCode() {
		int result = socket.hashCode();
		result = 31 * result + toSend.hashCode();
		result = 31 * result + disconnectedPipeline.hashCode();
		result = 31 * result + semaphore.hashCode();
		result = 31 * result + (setup ? 1 : 0);
		result = 31 * result + (started ? 1 : 0);
		result = 31 * result + session.hashCode();
		result = 31 * result + key.hashCode();
		result = 31 * result + threadPool.hashCode();
		result = 31 * result + sendingService.hashCode();
		result = 31 * result + receivingService.hashCode();
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Connection{" + key.getSimpleName() + "," + getFormattedAddress() + "}";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void acquire() throws InterruptedException {
		semaphore.acquire();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void release() {
		semaphore.release();
	}	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void finalize() throws Throwable {
		Logging.unified().debug("Connection ist collected by the GC ..");
		for (Object o : toSend) {
			Logging.unified().warn("LeftOver-Object " + o + " at dead connection!");
		}
		if (! socket.isClosed()) {
			socket.shutdownInput();
			socket.shutdownOutput();
			socket.close();
		}
		super.finalize();
	}

	/**
	 * {@inheritDoc}
	 */
	private class DefaultReceiveCallback implements Callback<Object> {
		@Override
		public boolean isRemovable() {
			return ! started;
		}

		@Override
		public void accept(final Object o) {
			receivedObject(o);
		}

		@Override
		public String toString() {
			return "DefaultReceiveCallback{removable=" + ! started + "}";
		}
	}




}
