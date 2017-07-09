package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.annotations.Asynchronous;
import de.thorbenkuck.netcom2.annotations.Synchronized;
import de.thorbenkuck.netcom2.exceptions.CommunicationNotSpecifiedException;
import de.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;
import de.thorbenkuck.netcom2.network.client.DecryptionAdapter;
import de.thorbenkuck.netcom2.network.client.DefaultSynchronize;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.interfaces.ReceivingService;
import de.thorbenkuck.netcom2.network.shared.Awaiting;
import de.thorbenkuck.netcom2.network.shared.CallBack;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.Synchronize;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Synchronized
class DefaultReceivingService implements ReceivingService {

	private final DecryptionAdapter decryptionAdapter;
	private final List<CallBack<Object>> callBacks = new ArrayList<>();
	private final Synchronize synchronize = new DefaultSynchronize(1);
	private final ExecutorService threadPool = Executors.newCachedThreadPool();
	private Runnable onDisconnect = () -> {
	};
	private Connection connection;
	private Session session;
	private Scanner in;
	private CommunicationRegistration communicationRegistration;
	private DeSerializationAdapter<String, Object> deSerializationAdapter;
	private Set<DeSerializationAdapter<String, Object>> fallBackDeSerialization;
	private boolean running = false;
	private Logging logging = Logging.unified();

	DefaultReceivingService(CommunicationRegistration communicationRegistration,
							DeSerializationAdapter<String, Object> deSerializationAdapter,
							Set<DeSerializationAdapter<String, Object>> fallBackDeSerialization,
							DecryptionAdapter decryptionAdapter) {
		this.communicationRegistration = communicationRegistration;
		this.deSerializationAdapter = deSerializationAdapter;
		this.fallBackDeSerialization = fallBackDeSerialization;
		this.decryptionAdapter = decryptionAdapter;
	}

	@Override
	public synchronized void run() {
		running = true;
		logging.debug("Started ReceivingService for " + connection.getKey() + "@" + connection.getFormattedAddress());
		synchronize.goOn();
		while (running()) {
			try {
				String string = in.nextLine();
				// First get, then execute!
				threadPool.submit(() -> handle(string));
			} catch (NoSuchElementException e) {
				logging.info("Disconnection detected!");
				softStop();
			} catch (Throwable throwable) {
				logging.error("Encountered unexpected Throwable while reading nextLine!", throwable);
			}
		}
		onDisconnect();
		logging.trace("Receiving Service stopped!");
	}

	@Override
	public void softStop() {
		running = false;
	}

	private void onDisconnect() {
		logging.info("Shutting down ReceivingService!");
		onDisconnect.run();
	}

	@Override
	public boolean running() {
		return running;
	}

	@Asynchronous
	private void handle(String string) {
		logging.trace("Handling " + string + " ..");
		try {
			logging.trace("Decrypting " + string);
			String toHandle = decrypt(string);
			logging.trace("Deserialize " + toHandle + " ..");
			Object object = deserialize(toHandle);
			logging.debug("Received: " + object + " at Connection " + connection.getKey() + "@" + connection.getFormattedAddress());
			Objects.requireNonNull(object);
			logging.trace("Triggering Communication ..");
			trigger(object);
			logging.trace("Notifying Callbacks ..");
			callBack(object);
		} catch (DeSerializationFailedException e) {
			logging.error("Could not Serialize!", e);
		}
	}

	private String decrypt(String s) {
		return decryptionAdapter.get(s);
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
		try {
			communicationRegistration.trigger(object.getClass(), connection, session, object);
		} catch (CommunicationNotSpecifiedException e) {
			logging.catching(e);
		}
	}

	private void callBack(Object object) {
		logging.debug("Accepting CallBacks(" + object + ")!");
		runSynchronizedOverCallbacks(() -> {
			logging.trace("Calling all callbacks, that want to be called ..");
			callBacks.stream()
					.filter(callBack -> callBack.isAcceptable(object))
					.forEach(callBack -> {
						logging.trace("Calling " + callBack + " ..");
						callBack.accept(object);
					});
		});

		cleanUpCallBacks();
	}

	private void runSynchronizedOverCallbacks(Runnable runnable) {
		logging.trace("Awaiting ThreadAccess over callBacks ..");
		synchronized (callBacks) {
			logging.trace("Acquired ThreadAccess over callBacks!");
			runnable.run();
		}
	}

	@Override
	public void cleanUpCallBacks() {
		logging.debug("CallBack cleanup requested!");
		List<CallBack<Object>> toRemove = new ArrayList<>();
		callBacks.stream()
				.filter(CallBack::isRemovable)
				.forEach(callBack -> {
					logging.trace("Marking CallBack " + callBack + " as isRemovable ..");
					toRemove.add(callBack);
				});
		runSynchronizedOverCallbacks(() -> toRemove.forEach(this::removeCallback));
		logging.debug("CallBack cleanup done!");
	}

	@Override
	public void addReceivingCallback(CallBack<Object> callBack) {
		logging.debug("Trying to add CallBack " + callBack);
		runSynchronizedOverCallbacks(() -> callBacks.add(callBack));
		logging.trace("Added Callback: " + callBack);
	}

	@Override
	public void setup(Connection connection, Session session) {
		this.connection = connection;
		this.session = session;
		try {
			synchronized (this) {
				in = new Scanner(connection.getInputStream());
			}
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	@Override
	public void setSession(Session session) {
		this.session = session;
	}

	@Override
	public void onDisconnect(Runnable runnable) {
		this.onDisconnect = runnable;
	}

	@Override
	public Awaiting started() {
		return synchronize;
	}

	@Asynchronous
	private void removeCallback(CallBack<Object> toRemove) {
		logging.trace("Preparing to isRemovable CallBack: " + toRemove);
		toRemove.onRemove();
		logging.debug("Removing CallBack " + toRemove);
		callBacks.remove(toRemove);
	}

}
