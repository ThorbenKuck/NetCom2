package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.exceptions.CommunicationNotSpecifiedException;
import com.github.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;
import com.github.thorbenkuck.netcom2.network.client.DecryptionAdapter;
import com.github.thorbenkuck.netcom2.network.client.DefaultSynchronize;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.interfaces.ReceivingService;
import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.CallBack;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.Synchronize;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;

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

	DefaultReceivingService(final CommunicationRegistration communicationRegistration,
							final DeSerializationAdapter<String, Object> deSerializationAdapter,
							final Set<DeSerializationAdapter<String, Object>> fallBackDeSerialization,
							final DecryptionAdapter decryptionAdapter) {
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
				final String string = in.nextLine();
				// First get, then execute!
				threadPool.submit(() -> handle(string));
			} catch (NoSuchElementException e) {
				logging.info("Disconnection detected!");
				softStop();
			}
		}
		onDisconnect();
		logging.trace("Receiving Service stopped!");
	}

	@Asynchronous
	private void handle(final String string) {
		logging.trace("Handling " + string + " ..");
		Object object = null;
		try {
			logging.trace("Decrypting " + string);
			String toHandle = decrypt(string);
			logging.trace("Deserialize " + toHandle + " ..");
			object = deserialize(toHandle);
			logging.debug("Received: " + object + " at Connection " + connection.getKey() + "@" + connection.getFormattedAddress());
			Objects.requireNonNull(object);
			logging.trace("Triggering Communication ..");
			trigger(object);
			logging.trace("Notifying Callbacks ..");
			callBack(object);
		} catch (final DeSerializationFailedException e) {
			logging.error("Could not Serialize!", e);
		} catch (final Throwable throwable) {
			logging.error("Encountered unexpected Throwable while handling " + (object != null ? object : string) + "!", throwable);
		}
	}

	@Override
	public void softStop() {
		running = false;
	}

	private void onDisconnect() {
		logging.info("Shutting down ReceivingService!");
		onDisconnect.run();
	}

	private String decrypt(final String s) {
		return decryptionAdapter.get(s);
	}

	private Object deserialize(final String string) throws DeSerializationFailedException {
		final String toDeserialize = decryptionAdapter.get(string);
		final DeSerializationFailedException deSerializationFailedException;
		try {
			return deSerializationAdapter.get(toDeserialize);
		} catch (final DeSerializationFailedException ex) {
			deSerializationFailedException = new DeSerializationFailedException(ex);
			for (final DeSerializationAdapter<String, Object> adapter : fallBackDeSerialization) {
				try {
					return adapter.get(toDeserialize);
				} catch (final DeSerializationFailedException e) {
					deSerializationFailedException.addSuppressed(e);
				}
			}
		}
		throw new DeSerializationFailedException(deSerializationFailedException);
	}

	private void trigger(final Object object) {
		try {
			try {
				communicationRegistration.acquire();
				communicationRegistration.trigger(object.getClass(), connection, session, object);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			} finally {
				communicationRegistration.release();
			}
		} catch (final CommunicationNotSpecifiedException e) {
			logging.catching(e);
		}
	}

	private void callBack(final Object object) {
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

	private void runSynchronizedOverCallbacks(final Runnable runnable) {
		logging.trace("Awaiting ThreadAccess over callBacks ..");
		synchronized (callBacks) {
			logging.trace("Acquired ThreadAccess over callBacks!");
			runnable.run();
		}
	}

	@Override
	public void cleanUpCallBacks() {
		logging.debug("CallBack cleanup requested!");
		final List<CallBack<Object>> toRemove = new ArrayList<>();
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
	public void addReceivingCallback(final CallBack<Object> callBack) {
		logging.debug("Trying to add CallBack " + callBack);
		runSynchronizedOverCallbacks(() -> callBacks.add(callBack));
		logging.trace("Added Callback: " + callBack);
	}

	@Override
	public void setup(final Connection connection, final Session session) {
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
	public void setSession(final Session session) {
		this.session = session;
	}

	@Override
	public void onDisconnect(final Runnable runnable) {
		this.onDisconnect = runnable;
	}

	@Override
	public Awaiting started() {
		return synchronize;
	}

	@Override
	public boolean running() {
		return running;
	}

	@Asynchronous
	private void removeCallback(final CallBack<Object> toRemove) {
		logging.trace("Preparing to isRemovable CallBack: " + toRemove);
		toRemove.onRemove();
		logging.debug("Removing CallBack " + toRemove);
		callBacks.remove(toRemove);
	}

}
