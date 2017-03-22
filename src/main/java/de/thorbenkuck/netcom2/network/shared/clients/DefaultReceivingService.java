package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.exceptions.CommunicationNotSpecifiedException;
import de.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;
import de.thorbenkuck.netcom2.interfaces.SimpleFactory;
import de.thorbenkuck.netcom2.logging.LoggingUtil;
import de.thorbenkuck.netcom2.network.client.DecryptionAdapter;
import de.thorbenkuck.netcom2.network.interfaces.ReceivingService;
import de.thorbenkuck.netcom2.network.shared.User;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import de.thorbenkuck.netcom2.network.shared.comm.model.Ping;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.Set;

class DefaultReceivingService implements ReceivingService {

	private final Socket socket;
	private final DecryptionAdapter decryptionAdapter;
	private final SimpleFactory<User> getUser;
	private final Runnable onAck;
	private final Runnable onDisconnect;
	private CommunicationRegistration communicationRegistration;
	private DeSerializationAdapter<String, Object> deSerializationAdapter;
	private Set<DeSerializationAdapter<String, Object>> fallBackDeSerialization;
	private Scanner in;
	private boolean running = false;
	private LoggingUtil logging = new LoggingUtil();

	DefaultReceivingService(Socket socket, CommunicationRegistration communicationRegistration,
							DeSerializationAdapter<String, Object> deSerializationAdapter,
							Set<DeSerializationAdapter<String, Object>> fallBackDeSerialization,
							DecryptionAdapter decryptionAdapter, SimpleFactory<User> getUser,
							Runnable onAck, Runnable onDisconnect) {
		this.socket = socket;
		this.communicationRegistration = communicationRegistration;
		this.deSerializationAdapter = deSerializationAdapter;
		this.fallBackDeSerialization = fallBackDeSerialization;
		this.decryptionAdapter = decryptionAdapter;
		this.getUser = getUser;
		this.onAck = onAck;
		this.onDisconnect = onDisconnect;
		try {
			in = new Scanner(socket.getInputStream());
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	@Override
	public void run() {
		logging.trace("Started ReceivingService for " + socket.getInetAddress() + ":" + socket.getPort());
		running = true;
		while (running()) {
			try {
				String string = in.nextLine();
				Object object = deserialize(string);
				logging.debug("Received: " + object);
				trigger(object);
			} catch (DeSerializationFailedException e) {
				logging.catching(e);
			} catch (Throwable throwable) {
				logging.catching(throwable);
				softStop();
			}
		}
		onDisconnect();
	}

	private Object deserialize(String string) throws DeSerializationFailedException {
		String toDeserialize = decryptionAdapter.get(string);
		DeSerializationFailedException deSerializationFailedException;
		try {
			return deSerializationAdapter.get(toDeserialize);
		} catch (DeSerializationFailedException ex) {
			deSerializationFailedException = new DeSerializationFailedException(ex);
			for (DeSerializationAdapter<String, Object> adapter : fallBackDeSerialization) {
				try {
					return adapter.get(toDeserialize);
				} catch (DeSerializationFailedException e) {
					deSerializationFailedException.addSuppressed(e);
				}
			}
		}
		throw new DeSerializationFailedException(deSerializationFailedException);
	}

	private void trigger(Object object) {
		if (object.getClass().equals(Ping.class)) {
			logging.trace("Ping requested");
			onAck.run();
		} else {
			try {
				communicationRegistration.trigger(object.getClass(), getUser.create(), object);
			} catch (CommunicationNotSpecifiedException e) {
				logging.catching(e);
			}
		}
	}

	@Override
	public void softStop() {
		running = false;
	}

	private void onDisconnect() {
		onDisconnect.run();
	}

	@Override
	public boolean running() {
		return running;
	}
}
