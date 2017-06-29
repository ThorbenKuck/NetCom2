package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.network.client.DefaultSynchronize;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.interfaces.ReceivingService;
import de.thorbenkuck.netcom2.network.interfaces.SendingService;
import de.thorbenkuck.netcom2.network.shared.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * DefaultConnection
 */
public class DefaultConnection implements Connection {

	private final Socket socket;
	private final LinkedBlockingQueue<Object> toSend = new LinkedBlockingQueue<>();
	private final Pipeline<Connection> disconnectedPipeline = new QueuedPipeline<>();
	private final ExecutorService threadPool = Executors.newFixedThreadPool(3);
	private boolean setup = false;
	private Logging logging = Logging.unified();
	private Session session;
	private ReceivingService receivingService;
	private SendingService sendingService;
	private boolean started = false;
	private Class<?> key;

	public DefaultConnection(Socket socket, Session session, ReceivingService receivingService, SendingService sendingService, Class<?> key) {
		this.socket = socket;
		this.session = session;
		this.receivingService = receivingService;
		this.sendingService = sendingService;
		this.key = key;
		setup();
	}

	private void setup() {
		logging.debug("Connection setup for " + socket);
		try {
			logging.trace("SendingService setup..");
			this.sendingService.setup(socket.getOutputStream(), toSend);
			logging.trace("SendingService was successfully setup!");
			logging.trace("ReceivingService setup..");
			this.receivingService.setup(this, getSession());
			logging.trace("ReceivingService was successfully setup!");
		} catch (IOException e) {
			try {
				logging.warn("Encountered Exception while ConnectionSetup!");
				logging.catching(e);
				close();
			} catch (IOException e1) {
				e1.addSuppressed(e);
				logging.fatal("Encountered Exception while cleaning up over a previously encountered Exception!", e1);
				throw new Error(e1);
			}
			throw new Error(e);
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
	}

	@Override
	public Synchronize startListening() {
		if (! setup) {
			throw new IllegalStateException("Connection has to be setup to listen!");
		}
		if (started) {
			throw new IllegalStateException("Cannot startListening to an already listening Connection");
		}

		Synchronize synchronize = new DefaultSynchronize(1);
		logging.debug("Starting to listen to: " + this);
		threadPool.submit(() -> {
			try {
				logging.trace("Awaiting Synchronization of ReceivingService");
				receivingService.started().synchronize();
				logging.trace("Awaiting Synchronization of SendingService");
				sendingService.started().synchronize();
			} catch (InterruptedException e) {
				logging.catching(e);
			}
			logging.info("Synchronization complete! Connection is now listening.");
			started = true;
			logging.trace("Realising awaiting Threads..");
			synchronize.goOn();
		});
		logging.trace("Executing ReceivingService ..");
		threadPool.submit(receivingService);
		logging.trace("Executing SendingService ..");
		threadPool.submit(sendingService);

		return synchronize;
	}

	@Override
	public Awaiting receivingOfClass(Class clazz) {
		Synchronize synchronize = new DefaultSynchronize(1);
		// TODO Auslagern
		receivingService.addReceivingCallback(new CallBack<Object>() {
			private boolean finished;

			@Override
			public void accept(Object o) {
				logging.trace("Realising Awaiting for " + clazz);
				synchronize.goOn();
				finished = true;
			}

			@Override
			public boolean isAcceptable(Object o) {
				return o != null && o.getClass().equals(clazz);
			}

			@Override
			public boolean remove() {
				return finished;
			}
		});

		return synchronize;
	}

	@Override
	public PipelineCondition<Connection> addOnDisconnectedConsumer(Consumer<Connection> consumer) {
		logging.debug("Added DisconnectedConsumer(" + consumer + ") for Connection " + this);
		return disconnectedPipeline.addLast(consumer);
	}

	@Override
	public void removeOnDisconnectedConsumer(Consumer<Connection> consumer) {
		logging.debug("Removed DisconnectedConsumer(" + consumer + ") from Connection " + this);
		disconnectedPipeline.remove(consumer);
	}

	@Override
	public void writeObject(Object object) {
		if (! setup) {
			throw new IllegalStateException("Connection has to be setup to send objects!");
		}
		logging.trace("Offering object " + object + " to write..");
		toSend.offer(object);
	}

	@Override
	public void addListener(Feasible<Class> feasible) {
		logging.debug("Added Feasible " + feasible);
		receivingService.addReceivingCallback(new CallBackFeasibleWrapper(feasible));
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return socket.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return socket.getOutputStream();
	}

	@Override
	public final LinkedBlockingQueue<Object> getSendInterface() {
		return toSend;
	}

	@Override
	public final Session getSession() {
		return session;
	}

	@Override
	public void setSession(Session session) {
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
	public void setKey(Class<?> connectionKey) {
		this.key = connectionKey;
	}

	@Override
	public void setLogging(Logging logging) {
		this.logging.debug("Overriding set Logging ..");
		this.logging = logging;
		logging.debug("Overrode Logging!");
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || ! o.getClass().equals(DefaultConnection.class)) {
			return false;
		}
		return ((DefaultConnection) o).socket.equals(socket);
	}

	@Override
	public String toString() {
		return "Connection at: " + getFormattedAddress();
	}


	@Override
	protected void finalize() {
		if (toSend != null) {
			for (Object o : toSend) {
				Logging.unified().warn("LeftOver-Object " + o + " at dead-connection!");
			}
		}
	}
}
