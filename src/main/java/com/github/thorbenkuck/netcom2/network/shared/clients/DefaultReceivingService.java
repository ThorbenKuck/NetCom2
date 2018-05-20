package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.annotations.Tested;
import com.github.thorbenkuck.netcom2.exceptions.CommunicationNotSpecifiedException;
import com.github.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;
import com.github.thorbenkuck.netcom2.exceptions.SetupListenerException;
import com.github.thorbenkuck.netcom2.network.interfaces.DecryptionAdapter;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.interfaces.ReceivingService;
import com.github.thorbenkuck.netcom2.network.shared.*;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

/**
 * {@inheritDoc}
 *
 * @version 1.0
 * @since 1.0
 */
@APILevel
@Tested(responsibleTest = "com.github.thorbenkuck.netcom2.network.shared.clients.DefaultReceivingServiceTest")
class DefaultReceivingService implements ReceivingService {

	@APILevel
	protected final List<Callback<Object>> callbacks = new ArrayList<>();
	private final Supplier<DecryptionAdapter> decryptionAdapter;
	private final Synchronize synchronize = Synchronize.create();
	private final Supplier<DeSerializationAdapter<String, Object>> deSerializationAdapter;
	private final Supplier<Set<DeSerializationAdapter<String, Object>>> fallBackDeSerialization;
	private Runnable onDisconnect = () -> {
	};
	private Connection connection;
	private Session session;
	private DataInputStream in;
	private CommunicationRegistration communicationRegistration;
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
	 * Handles an received String, with all needed steps.
	 * <p>
	 * This, in fact means the following:
	 * <p>
	 * <ul>
	 * <li>decrypt the String</li>
	 * <li>deserialize the String</li>
	 * <li>triggers the {@link CommunicationRegistration} with the de serialized object</li>
	 * <li>triggers all {@link Callback} instances with the de serialized object</li>
	 * </ul>
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
			logging.info("[ReceivingService] Received " + object);
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
	 * Triggers the DisconnectedRunnable
	 */
	private void onDisconnect() {
		logging.info("[ReceivingService] Shutting down ReceivingService!");
		onDisconnect.run();
		running = false;
	}

	/**
	 * Decrypts the provided String.
	 *
	 * @param s the encrypted String.
	 * @return the decrypted String.
	 */
	private String decrypt(final String s) {
		return decryptionAdapter.get().get(s);
	}

	/**
	 * Deserialize the provided String.
	 * <p>
	 * This will firstly ask the set MainSerializationAdapter.
	 * <p>
	 * Will ask all DeserializationAdapters if the main Adapter fails.
	 *
	 * @param string the received String.
	 * @return the Object for that String.
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
	 * Triggers the {@link CommunicationRegistration} with the needed Objects.
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
	 * Triggers Callbacks, listening to the receiving of any Object.
	 * <p>
	 * Afterwards, the Callbacks will be cleaned to free up resources.
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
	 * Runs a Runnable synchronized over the Callbacks.
	 *
	 * @param runnable the Runnable.
	 */
	private void runSynchronizedOverCallbacks(final Runnable runnable) {
		logging.trace("[ReceivingService] Awaiting ThreadAccess over callbacks ..");
		synchronized (callbacks) {
			logging.trace("[ReceivingService] Acquired ThreadAccess over callbacks!");
			runnable.run();
		}
	}

	/**
	 * Removes a {@link Callback}.
	 *
	 * @param toRemove the provided callback.
	 */
	@Asynchronous
	private void removeCallback(final Callback<Object> toRemove) {
		logging.trace("[ReceivingService] Preparing to remove Callback: " + toRemove);
		toRemove.onRemove();
		logging.debug("[ReceivingService] Removing Callback " + toRemove);
		callbacks.remove(toRemove);
	}

	private int readNext(DynamicBuffer callbackl, Buffer buffer) throws IOException {
		logging.trace("[ReceivingService] Waiting for available data (blocking)");
		int read = in.read(buffer.array());
		logging.trace("[ReceivingService] Read result: " + read);
		if (read == -1) {
			logging.trace("[ReceivingService] EOF reached! SoftStop initialized. Notifying ..");
			softStop();
			return -1;
		}
		logging.trace("[ReceivingService] Appending read data");
		callbackl.append(buffer.array());
		logging.trace("[ReceivingService] Clearing Buffer");
		buffer.clear();

		return read;
	}

	private Optional<String> readBlocking() throws IOException {
		logging.debug("[ReceivingService] Starting to read.");
		logging.trace("[ReceivingService] Allocating Buffer ..");
		final Buffer buffer = new Buffer(256);
		logging.trace("[ReceivingService] Preparing StringBuilder as callback");
		final DynamicBuffer callback = new DynamicBuffer();
		// This call blocks. Therefor
		// if we continue past this point
		// we WILL have some sort of
		// result. This might be -1, which
		// means, EOF (disconnect.)
		logging.trace("[ReceivingService] Reading from Socket ..");
		if (readNext(callback, buffer) == -1) {
			logging.trace("[ReceivingService] Read EOF. Disconnecting!");
			return Optional.empty();
		}
		logging.trace("[ReceivingService] Reading successful. Attempting to check for more available data ..");
		while (in.available() > 0) {
			logging.trace("[ReceivingService] Found " + in.available() + " more bytes in InputStream.");
			logging.trace("[ReceivingService] Adjusting Buffer size ..");
			buffer.reallocate(in.available());
			if (readNext(callback, buffer) == -1) {
				logging.trace("[ReceivingService] Read EOF. Disconnecting!");
				return Optional.empty();
			}
			logging.trace("[ReceivingService] Reading successful. Attempting to check for more available data ..");
		}
		logging.trace("[ReceivingService] Read all data. Tearing down buffer to free up memory ..");

		buffer.teardown();

		logging.trace("[ReceivingService] Reading done.");
		return Optional.of(new String(callback.array()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
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
				logging.trace("[ReceivingService] Initializing reading of InputStream");
				final Optional<String> rawDataOptional = readBlocking();
				// First get, then execute!
				// Not in one line, so that the
				// get part is executed in this thread
				logging.trace("[ReceivingService] Extracting raw data to be handled.");
				rawDataOptional.ifPresent(string -> NetCom2Utils.runOnNetComThread(() -> handle(string)));

			} catch (NoSuchElementException | IOException e) {
				if (running()) {
					logging.catching(e);
				}
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
	 *
	 * @throws IllegalArgumentException if the provided callback is null
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
	 *
	 * @throws IllegalArgumentException if the connection or the session is null
	 */
	@Override
	public void setup(final Connection connection, final Session session) {
		NetCom2Utils.parameterNotNull(connection, session);
		this.connection = connection;
		this.session = session;
		try {
			synchronized (this) {
				in = new DataInputStream(connection.getInputStream());
			}
			setup = true;
		} catch (IOException e) {
			throw new SetupListenerException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the session is null
	 */
	@Override
	public void setSession(final Session session) {
		NetCom2Utils.parameterNotNull(session);
		this.session = session;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the runnable is null
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
