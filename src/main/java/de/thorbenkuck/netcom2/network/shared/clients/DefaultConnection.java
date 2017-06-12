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

	public DefaultConnection(Socket socket, Session session, ReceivingService receivingService, SendingService sendingService) {
		this.socket = socket;
		this.session = session;
		this.receivingService = receivingService;
		this.sendingService = sendingService;
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
				logging.error("Encountered Exception while cleaning up over a previously encountered Exception!", e1);
				throw new Error(e1);
			}
			throw new Error(e);
		}
		setup = true;
	}

	@Override
	public void close() throws IOException {
		receivingService.softStop();
		sendingService.softStop();
		threadPool.shutdown();
		socket.close();
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
				logging.trace("Awaiting Synchronization of ReceivingService and SendingService");
				receivingService.started().synchronize();
				sendingService.started().synchronize();
			} catch (InterruptedException e) {
				logging.catching(e);
			}
			logging.info("Synchronization complete! Now i am listening!");
			started = true;
			synchronize.goOn();
		});
		threadPool.submit(receivingService);
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
			public boolean acceptable(Object o) {
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
		return disconnectedPipeline.addLast(consumer);
	}

	@Override
	public void removeOnDisconnectedConsumer(Consumer<Connection> consumer) {
		disconnectedPipeline.remove(consumer);
	}

	@Override
	public void writeObject(Object object) {
		if (! setup) {
			throw new IllegalStateException("Connection has to be setup to send objects!");
		}
		logging.trace("Offering object " + object + " to Send");
		toSend.offer(object);
	}

	@Override
	public void addListener(Feasible<Class> feasible) {
		receivingService.addReceivingCallback(new ConnectionCallBack(feasible));
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
		logging.warn("Overriding Session!");
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
	public boolean active() {
		return socket.isConnected();
	}

	@Override
	public void setLogging(Logging logging) {
		this.logging = logging;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || ! o.getClass().equals(DefaultConnection.class)) {
			return false;
		}
		return ((DefaultConnection) o).socket.equals(socket);
	}

	private class ConnectionCallBack implements CallBack<Object> {

		private final Feasible<Class> feasible;

		private ConnectionCallBack(Feasible<Class> feasible) {
			this.feasible = feasible;
		}

		@Override
		public void accept(Object object) {
			feasible.tryAccept(object.getClass());
		}

		@Override
		public boolean acceptable(Object object) {
			return feasible.acceptable(object);
		}

		@Override
		public boolean remove() {
			return feasible.remove();
		}

		public String toString() {
			return feasible.toString();
		}
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
