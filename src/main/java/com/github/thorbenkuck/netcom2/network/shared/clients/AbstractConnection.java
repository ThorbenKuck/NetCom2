package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.annotations.Experimental;
import com.github.thorbenkuck.netcom2.exceptions.ClientCreationFailedException;
import com.github.thorbenkuck.netcom2.interfaces.Mutex;
import com.github.thorbenkuck.netcom2.network.client.DefaultSynchronize;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.interfaces.ReceivingService;
import com.github.thorbenkuck.netcom2.network.interfaces.SendingService;
import com.github.thorbenkuck.netcom2.network.shared.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.function.Consumer;

public abstract class AbstractConnection implements Connection, Mutex {

	private final Socket socket;
	private final BlockingQueue<Object> toSend = new LinkedBlockingQueue<>();
	private final Pipeline<Connection> disconnectedPipeline = new QueuedPipeline<>();
	private final Semaphore semaphore = new Semaphore(1);
	protected Logging logging = Logging.unified();
	protected SendingService sendingService;
	protected ReceivingService receivingService;
	private boolean setup;
	private boolean started;
	private Session session;
	private Class<?> key;
	private ExecutorService threadPool = Executors.newCachedThreadPool();

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

	protected abstract void onClose();

	@Override
	public void setLogging(final Logging logging) {
		this.logging.debug("Overriding set Logging ..");
		this.logging = logging;
		logging.debug("Overrode Logging!");
	}

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

	@Override
	public void close() throws IOException {
		logging.debug("Closing Connection " + this);
		logging.trace("Requesting soft-stop of set ReceivingService ..");
		receivingService.softStop();
		logging.trace("Requesting soft-stop of set SendingService ..");
		sendingService.softStop();
		logging.trace("Requesting soft-stop of ThreadPool ..");
		logging.info("Sending Service will be shut down forcefully! Expect an InterruptedException!");
		threadPool.shutdownNow();
		logging.trace("Shutting down socket ..");
		socket.close();
		logging.debug("Successfully shut down Connection " + this);
		onClose();
	}

	@Override
	public Synchronize startListening() {
		if (!setup) {
			throw new IllegalStateException("Connection has to be setup to listen!");
		}
		if (started) {
			throw new IllegalStateException("Cannot startListening to an already listening Connection");
		}

		final Synchronize synchronize = new DefaultSynchronize();
		logging.debug("Starting to listen to: " + this);
		threadPool.submit(() -> {
			try {
				logging.trace("Awaiting Synchronization of ReceivingService");
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

	@Override
	public PipelineCondition<Connection> addOnDisconnectedConsumer(final Consumer<Connection> consumer) {
		logging.debug("Added DisconnectedConsumer(" + consumer + ") for Connection " + this);
		return disconnectedPipeline.addLast(consumer);
	}

	@Override
	public void removeOnDisconnectedConsumer(final Consumer<Connection> consumer) {
		logging.debug("Removed DisconnectedConsumer(" + consumer + ") from Connection " + this);
		disconnectedPipeline.remove(consumer);
	}

	@Asynchronous
	@Override
	public void write(final Object object) {
		if (!setup) {
			throw new IllegalStateException("Connection has to be setup to beforeSend objects!");
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

	@Override
	public final InputStream getInputStream() throws IOException {
		return socket.getInputStream();
	}

	@Override
	public final OutputStream getOutputStream() throws IOException {
		return socket.getOutputStream();
	}

	@Override
	public void addObjectSendListener(final Callback<Object> callback) {
		logging.trace("Adding SendCallback " + callback + " to " + this);
		sendingService.addSendDoneCallback(callback);
	}

	@Override
	public void addObjectReceivedListener(final Callback<Object> callback) {
		logging.trace("Adding ReceiveCallback " + callback + " to " + this);
		receivingService.addReceivingCallback(callback);
	}

	@Override
	public final Session getSession() {
		return session;
	}

	@Override
	public void setSession(final Session session) {
		logging.debug("Overriding Session for " + this);
		receivingService.setSession(session);
		this.session = session;
	}

	@Override
	public String getFormattedAddress() {
		return getInetAddress() + ":" + getPort();
	}

	@Override
	public int getPort() {
		return socket.getPort();
	}

	@Override
	public InetAddress getInetAddress() {
		return socket.getInetAddress();
	}

	@Override
	public boolean isActive() {
		return socket.isConnected();
	}

	@Override
	public Class<?> getKey() {
		return key;
	}

	@Override
	public void setKey(final Class<?> connectionKey) {
		this.key = connectionKey;
	}

	/**
	 * TODO Complete the formed out Algorithm
	 *
	 * @param executorService the executorSerive
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
		sendingService.softStop();
		receivingService.softStop();
		threadPool.shutdownNow();
		this.threadPool = executorService;
		try {
			startListening().synchronize();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		*/
	}

	@Override
	public BlockingQueue<Object> getSendInterface() {
		return toSend;
	}

	@Override
	public boolean equals(final Object o) {
		return o != null && o.getClass().equals(AbstractConnection.class) &&
				((AbstractConnection) o).socket.equals(socket);
	}

	protected abstract void afterSend(final Object o);

	@Override
	public String toString() {
		return "Connection{" + key.getSimpleName() + "," + getFormattedAddress() + "}";
	}

	@Override
	protected void finalize() throws Throwable {
		Logging.unified().debug("Connection ist collected by the GC ..");
		for (Object o : toSend) {
			Logging.unified().warn("LeftOver-Object " + o + " at dead connection!");
		}
		super.finalize();
	}

	@Override
	public void acquire() throws InterruptedException {
		semaphore.acquire();
	}

	@Override
	public void release() {
		semaphore.release();
	}

	private class DefaultReceiveCallback implements Callback<Object> {
		@Override
		public boolean isRemovable() {
			return !started;
		}

		@Override
		public void accept(final Object o) {
			receivedObject(o);
		}

		@Override
		public String toString() {
			return "DefaultReceiveCallback{removable=" + !started + "}";
		}
	}
}
