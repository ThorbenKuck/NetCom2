package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.exceptions.CommunicationNotSpecifiedException;
import com.github.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;
import com.github.thorbenkuck.netcom2.exceptions.SetupListenerException;
import com.github.thorbenkuck.netcom2.network.interfaces.DecryptionAdapter;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.interfaces.ReceivingService;
import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.Callback;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.Synchronize;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.synchronization.DefaultSynchronize;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

@APILevel
@Synchronized
class DefaultReceivingService implements ReceivingService {

	@APILevel
	protected final List<Callback<Object>> callbacks = new ArrayList<>();
	private final Supplier<DecryptionAdapter> decryptionAdapter;
	private final Synchronize synchronize = new DefaultSynchronize(1);
	private Runnable onDisconnect = () -> {
	};
	private Connection connection;
	private Session session;
	private Scanner in;
	private CommunicationRegistration communicationRegistration;
	private Supplier<DeSerializationAdapter<String, Object>> deSerializationAdapter;
	private Supplier<Set<DeSerializationAdapter<String, Object>>> fallBackDeSerialization;
	private boolean running = false;
	private boolean setup = false;
	private Logging logging = Logging.unified();

	@APILevel
	DefaultReceivingService(final CommunicationRegistration communicationRegistration,
	                        final Supplier<DeSerializationAdapter<String, Object>> deSerializationAdapter,
	                        final Supplier<Set<DeSerializationAdapter<String, Object>>> fallBackDeSerialization,
	                        final Supplier<DecryptionAdapter> decryptionAdapter) {
		this.communicationRegistration = communicationRegistration;
		this.deSerializationAdapter = deSerializationAdapter;
		this.fallBackDeSerialization = fallBackDeSerialization;
		this.decryptionAdapter = decryptionAdapter;
	}

	/**
	 * Handles an received String, with all needed steps
	 *
	 * @param string the raw received Strings.
	 */
	@Asynchronous
	private void handle(final String string) {
		logging.trace("[ReceivingService] Handling " + string + " ..");
		Object object = null;
		try {
			logging.trace("[ReceivingService] Decrypting " + string);
			String toHandle = decrypt(string);
			logging.trace("[ReceivingService] Deserialize " + toHandle + " ..");
			object = deserialize(toHandle);
			logging.debug("[ReceivingService] Received: " + object + " at Connection " + connection.getKey() + "@" +
					connection.getFormattedAddress());
			NetCom2Utils.assertNotNull(object);
			logging.trace("[ReceivingService] Triggering Communication ..");
			trigger(object);
			logging.trace("[ReceivingService] Notifying Callbacks ..");
			callBack(object);
		} catch (final DeSerializationFailedException e) {
			logging.error("[ReceivingService] Could not Serialize!", e);
		} catch (final Throwable throwable) {
			logging.error("[ReceivingService] Encountered unexpected Throwable while handling " + (object != null ? object : string) + "!",
					throwable);
		}
	}

	/**
	 * Triggers the disconnectedRunnable
	 */
	private void onDisconnect() {
		logging.info("[ReceivingService] Shutting down ReceivingService!");
		onDisconnect.run();
		running = false;
	}

	/**
	 * Decrypts the provided String.
	 *
	 * @param s the encrypted String
	 * @return the decrypted String
	 */
	private String decrypt(final String s) {
		return decryptionAdapter.get().get(s);
	}

	/**
	 * Deserialize the provided String.
	 * <p>
	 * Will ask all DeserializationAdapters if the main Adapter fails.
	 *
	 * @param string the received String
	 * @return the Object for that String
	 * @throws DeSerializationFailedException if no DeSerializationAdapter can handle the String
	 */
	private Object deserialize(final String string) throws DeSerializationFailedException {
		final String toDeserialize = decrypt(string);
		final DeSerializationFailedException deSerializationFailedException;
		try {
			return deSerializationAdapter.get().get(toDeserialize);
		} catch (final DeSerializationFailedException ex) {
			deSerializationFailedException = new DeSerializationFailedException(ex);
			for (final DeSerializationAdapter<String, Object> adapter : fallBackDeSerialization.get()) {
				try {
					return adapter.get(toDeserialize);
				} catch (final DeSerializationFailedException e) {
					deSerializationFailedException.addSuppressed(e);
				}
			}
		}
		throw new DeSerializationFailedException(deSerializationFailedException);
	}

	/**
	 * Triggers the CommunicationRegistration with the needed Objects.
	 *
	 * @param object the receivedObject.
	 * @see CommunicationRegistration#trigger(Connection, Session, Object)
	 */
	private void trigger(final Object object) {
		try {
			try {
				communicationRegistration.acquire();
				communicationRegistration.trigger(object.getClass(), connection, session, object);
			} catch (final InterruptedException e) {
				logging.catching(e);
			} finally {
				communicationRegistration.release();
			}
		} catch (final CommunicationNotSpecifiedException e) {
			logging.catching(e);
		}
	}

	/**
	 * Triggers Callbacks, listening to the Receiving of an Object.
	 * <p>
	 * Afterwards, the CallBacks will be cleaned to free up resources.
	 *
	 * @param object the Object we received.
	 */
	private void callBack(final Object object) {
		logging.debug("[ReceivingService] Accepting CallBacks(" + object + ")!");
		runSynchronizedOverCallbacks(() -> {
			logging.trace("[ReceivingService] Calling all callbacks, that want to be called ..");
			callbacks.stream()
					.filter(callBack -> callBack.isAcceptable(object))
					.forEach(callBack -> {
						logging.trace("[ReceivingService] Calling " + callBack + " ..");
						callBack.accept(object);
					});
		});

		cleanUpCallBacks();
	}

	/**
	 * Runs an Runnable synchronized over the Callbacks
	 *
	 * @param runnable the Runnable
	 */
	private void runSynchronizedOverCallbacks(final Runnable runnable) {
		logging.trace("[ReceivingService] Awaiting ThreadAccess over callbacks ..");
		synchronized (callbacks) {
			logging.trace("[ReceivingService] Acquired ThreadAccess over callbacks!");
			runnable.run();
		}
	}

	/**
	 * Removes a callback.
	 *
	 * @param toRemove the callback
	 */
	@Asynchronous
	private void removeCallback(final Callback<Object> toRemove) {
		logging.trace("[ReceivingService] Preparing to remove Callback: " + toRemove);
		toRemove.onRemove();
		logging.debug("[ReceivingService] Removing Callback " + toRemove);
		callbacks.remove(toRemove);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void run() {
		if (!isSetup()) {
			throw new SetupListenerException("[ReceivingService] has to be setup before running it!");
		}
		running = true;
		logging.debug("[ReceivingService] Started ReceivingService for " + connection.getKey() + "@" + connection.getFormattedAddress());
		synchronize.goOn();
		while (running()) {
			try {
				final String string = in.nextLine();
				// First get, then execute!
				// Not in one line, so that the
				// get part is executed in this thread
				NetCom2Utils.runOnNetComThread(() -> handle(string));
			} catch (NoSuchElementException e) {
				logging.info("[ReceivingService] Disconnection detected!");
				softStop();
			}
		}
		onDisconnect();
		logging.trace("[ReceivingService] Receiving Service stopped!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void softStop() {
		running = false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean running() {
		return running;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cleanUpCallBacks() {
		logging.debug("[ReceivingService] Callback cleanup requested!");
		final List<Callback<Object>> toRemove = new ArrayList<>();
		runSynchronizedOverCallbacks(() -> {
			callbacks.stream()
					.filter(Callback::isRemovable)
					.forEach(callBack -> {
						logging.trace("[ReceivingService] Marking Callback " + callBack + " as to be removed ..");
						toRemove.add(callBack);
					});
			toRemove.forEach(this::removeCallback);
		});

		logging.debug("[ReceivingService] Callback cleanup done!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addReceivingCallback(final Callback<Object> callback) {
		NetCom2Utils.parameterNotNull(callback);
		logging.debug("[ReceivingService] Trying to add Callback " + callback);
		runSynchronizedOverCallbacks(() -> callbacks.add(callback));
		logging.trace("[ReceivingService] Added Callback: " + callback);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setup(final Connection connection, final Session session) {
		NetCom2Utils.parameterNotNull(connection, session);
		this.connection = connection;
		this.session = session;
		try {
			synchronized (this) {
				in = new Scanner(connection.getInputStream());
				setup = true;
			}
		} catch (IOException e) {
			throw new SetupListenerException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSession(final Session session) {
		NetCom2Utils.parameterNotNull(session);
		this.session = session;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDisconnect(final Runnable runnable) {
		NetCom2Utils.parameterNotNull(runnable);
		this.onDisconnect = runnable;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Awaiting started() {
		return synchronize;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSetup() {
		return setup;
	}
}
