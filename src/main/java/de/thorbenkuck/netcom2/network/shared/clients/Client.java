package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.logging.LoggingUtil;
import de.thorbenkuck.netcom2.network.client.DecryptionAdapter;
import de.thorbenkuck.netcom2.network.client.EncryptionAdapter;
import de.thorbenkuck.netcom2.network.interfaces.ReceivingService;
import de.thorbenkuck.netcom2.network.interfaces.SendingService;
import de.thorbenkuck.netcom2.network.shared.DisconnectedHandler;
import de.thorbenkuck.netcom2.network.shared.User;
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
	private LoggingUtil logging = new LoggingUtil();
	private CountDownLatch primed = new CountDownLatch(1);
	private boolean invoked = false;
	private User user;

	public Client(Socket socket, CommunicationRegistration communicationRegistration) {
		this.socket = socket;
		this.communicationRegistration = communicationRegistration;
		setMainSerializationAdapter(new JavaSerializationAdapter());
		setMainDeSerializationAdapter(new JavaDeSerializationAdapter());
	}

	public void setMainSerializationAdapter(SerializationAdapter<Object, String> mainSerializationAdapter) {
		this.mainSerializationAdapter = mainSerializationAdapter;
	}

	public void setMainDeSerializationAdapter(DeSerializationAdapter<String, Object> mainDeSerializationAdapter) {
		this.mainDeSerializationAdapter = mainDeSerializationAdapter;
	}

	public void invoke() throws IOException {
		if (invoked) {
			return;
		}
		receivingService = new DefaultReceivingService(socket, communicationRegistration, mainDeSerializationAdapter,
				fallBackDeSerialization, decryptionAdapter, this::getUser, this::ack, this::disconnect);
		sendingService = new DefaultSendingService(toSend, mainSerializationAdapter, fallBackSerialization,
				new PrintWriter(socket.getOutputStream()), encryptionAdapter);
		start();
	}

	private void start() {
		threadPool = Executors.newFixedThreadPool(2);
		threadPool.execute(receivingService);
		threadPool.execute(sendingService);
		invoked = true;
		logging.trace(toString() + " successfully created!");
	}

	@Override
	public String toString() {
		return "Client{" +
				"user=" + user +
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

	public CountDownLatch getPrimed() {
		return primed;
	}

	public void send(Object object) {
		toSend.offer(object);
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void addFallBackSerialization(SerializationAdapter<Object, String> serializationAdapter) {
		fallBackSerialization.add(serializationAdapter);
	}

	public void addFallBackDeSerialization(DeSerializationAdapter<String, Object> deSerializationAdapter) {
		fallBackDeSerialization.add(deSerializationAdapter);
	}

	public void addDisconnectedHandler(DisconnectedHandler disconnectedHandler) {
		disconnectedHandlers.add(disconnectedHandler);
	}

	public boolean matchesWith(Object o) {
		if (o == null) {
			return false;
		}

		if (Socket.class.equals(o.getClass())) {
			return o.equals(socket);
		}
		return false;
	}
}