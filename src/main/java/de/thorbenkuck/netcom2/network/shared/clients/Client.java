package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.logging.LoggingUtil;
import de.thorbenkuck.netcom2.network.client.DecryptionAdapter;
import de.thorbenkuck.netcom2.network.client.EncryptionAdapter;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.interfaces.ReceivingService;
import de.thorbenkuck.netcom2.network.interfaces.SendingService;
import de.thorbenkuck.netcom2.network.shared.DisconnectedHandler;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Client {

	private final Socket socket;
	private final CommunicationRegistration communicationRegistration;
	private final LinkedBlockingQueue<Object> toSend = new LinkedBlockingQueue<>();
	private final List<DisconnectedHandler> disconnectedHandlers = new ArrayList<>();
	private SendingService sendingService;
	private EncryptionAdapter encryptionAdapter = s -> s;
	private DecryptionAdapter decryptionAdapter = s -> s;
	private ReceivingService receivingService;
	private SerializationAdapter<Object, String> mainSerializationAdapter;
	private DeSerializationAdapter<String, Object> mainDeSerializationAdapter;
	private Set<SerializationAdapter<Object, String>> fallBackSerialization = new HashSet<>();
	private Set<DeSerializationAdapter<String, Object>> fallBackDeSerialization = new HashSet<>();
	private ExecutorService threadPool;
	private Logging logging = new LoggingUtil();
	private CountDownLatch primed = new CountDownLatch(1);
	private boolean invoked = false;
	private Session session;

	public Client(Socket socket, CommunicationRegistration communicationRegistration) {
		this.socket = socket;
		this.communicationRegistration = communicationRegistration;
		setMainSerializationAdapter(new JavaSerializationAdapter());
		setMainDeSerializationAdapter(new JavaDeSerializationAdapter());
	}

	public final void setMainSerializationAdapter(SerializationAdapter<Object, String> mainSerializationAdapter) {
		this.mainSerializationAdapter = mainSerializationAdapter;
	}

	public final void setMainDeSerializationAdapter(DeSerializationAdapter<String, Object> mainDeSerializationAdapter) {
		this.mainDeSerializationAdapter = mainDeSerializationAdapter;
	}

	public final void invoke() throws IOException {
		if (invoked) {
			return;
		}
		logging.trace("Entered Client#invoke");
		receivingService = new DefaultReceivingService(socket, communicationRegistration, mainDeSerializationAdapter,
				fallBackDeSerialization, decryptionAdapter, this::getSession, this::ack, this::disconnect);
		sendingService = new DefaultSendingService(toSend, mainSerializationAdapter, fallBackSerialization,
				new PrintWriter(socket.getOutputStream()), encryptionAdapter);
		start();
		logging.trace("Leaving Client#invoke");
	}

	private void start() {
		threadPool = Executors.newFixedThreadPool(2);
		threadPool.execute(receivingService);
		threadPool.execute(sendingService);
		invoked = true;
		logging.debug(toString() + " successfully created!");
	}

	@Override
	public final String toString() {
		return "Client{" +
				"session=" + session +
				", address=" + socket.getInetAddress() + ":" + socket.getPort() +
				", mainSerializationAdapter=" + mainSerializationAdapter +
				", mainDeSerializationAdapter=" + mainDeSerializationAdapter +
				", fallBackSerialization=" + fallBackSerialization +
				", fallBackDeSerialization=" + fallBackDeSerialization +
				", decryptionAdapter" + decryptionAdapter +
				", encryptionAdapter" + encryptionAdapter +
				", invoked=" + invoked +
				'}';
	}

	private void disconnect() {
		disconnectedHandlers.sort(Comparator.comparingInt(DisconnectedHandler::getPriority));
		disconnectedHandlers.stream()
				.filter(DisconnectedHandler::active)
				.forEachOrdered(dh -> dh.handle(this));
	}

	private void ack() {
		logging.trace("Acknowledging");
		primed.countDown();
	}

	public final CountDownLatch getPrimed() {
		return primed;
	}

	public final void send(Object object) {
		toSend.offer(object);
	}

	public final Session getSession() {
		return session;
	}

	public final void setSession(Session session) {
		this.session = session;
	}

	public final void addFallBackSerialization(SerializationAdapter<Object, String> serializationAdapter) {
		fallBackSerialization.add(serializationAdapter);
	}

	public final void addFallBackDeSerialization(DeSerializationAdapter<String, Object> deSerializationAdapter) {
		fallBackDeSerialization.add(deSerializationAdapter);
	}

	public final void addDisconnectedHandler(DisconnectedHandler disconnectedHandler) {
		disconnectedHandlers.add(disconnectedHandler);
	}
}