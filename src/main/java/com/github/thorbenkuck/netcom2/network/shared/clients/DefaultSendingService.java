package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.exceptions.SerializationFailedException;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.client.DefaultSynchronize;
import com.github.thorbenkuck.netcom2.network.client.EncryptionAdapter;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.interfaces.SendingService;
import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.Callback;
import com.github.thorbenkuck.netcom2.network.shared.Synchronize;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@APILevel
class DefaultSendingService implements SendingService {

	private final SerializationAdapter<Object, String> mainSerializationAdapter;
	private final Set<SerializationAdapter<Object, String>> fallBackSerialization;
	private final EncryptionAdapter encryptionAdapter;
	private Supplier<String> connectionID = () -> "UNKNOWN-CONNECTION";
	private final Logging logging = new NetComLogging();
	private final Synchronize synchronize = new DefaultSynchronize(1);
	private final ExecutorService threadPool = NetCom2Utils.getNetComExecutorService();
	private final List<Callback<Object>> callbacks = new ArrayList<>();
	private PrintWriter printWriter;
	@APILevel
	private BlockingQueue<Object> toSend;
	private boolean running = false;
	private boolean setup = false;

	@APILevel
	DefaultSendingService(final SerializationAdapter<Object, String> mainSerializationAdapter,
						  final Set<SerializationAdapter<Object, String>> fallBackSerialization,
						  final EncryptionAdapter encryptionAdapter) {
		this.mainSerializationAdapter = mainSerializationAdapter;
		this.fallBackSerialization = fallBackSerialization;
		this.encryptionAdapter = encryptionAdapter;
	}

	@Override
	public void run() {
		if (!setup) {
			throw new Error("[SendingService{" + connectionID.get() + "}] Setup required before run!");
		}
		running = true;
		logging.debug("[SendingService{" + connectionID.get() + "}] Started Sending Service");
		synchronize.goOn();
		while (running()) {
			try {
				// This needs to be done with a timeout
				// if not, a shutdown may not be viable
				// and the thread this runnable runs in may never terminate
				final Object o = toSend.poll(2, TimeUnit.SECONDS);
				// Take it first, then send it in another thread!
				// this is done, to check, whether or not the object is null
				// if it is null, no object was present.
				// This means, the SendingService was shutDown.
				if(o != null) {
					threadPool.submit(() -> send(o));
				}
			} catch (InterruptedException e) {
				if (running) {
					logging.warn("[SendingService{" + connectionID.get() + "}] Interrupted while waiting for a new Object to beforeSend");
					logging.catching(e);
				}
			}
		}
		logging.info("[SendingService{" + connectionID.get() + "}] SendingService stopped!");
	}

	@Override
	public void setConnectionIDSupplier(Supplier<String> supplier) {
		this.connectionID = supplier;
	}

	private void send(final Object o) {
		try {
			logging.debug("[SendingService{" + connectionID.get() + "}] Sending " + o + " ..");
			logging.trace("[SendingService{" + connectionID.get() + "}] Serializing " + o + " ..");
			String toSend = serialize(o);
			logging.trace("[SendingService{" + connectionID.get() + "}] Encrypting " + toSend + " ..");
			toSend = encrypt(toSend);
			logging.trace("[SendingService{" + connectionID.get() + "}] Writing: " + toSend + " ..");
			printWriter.println(encryptionAdapter.get(toSend));
			printWriter.flush();
			logging.trace("[SendingService{" + connectionID.get() + "}] Successfully wrote " + toSend + "!");
			logging.trace("[SendingService{" + connectionID.get() + "}] Accepting CallBacks ..");
			triggerCallbacks(o);
		} catch (SerializationFailedException e) {
			logging.error("[SendingService{" + connectionID.get() + "}] Failed to Serialize!", e);
		} catch (Throwable throwable) {
			logging.error("[SendingService{" + connectionID.get() + "}] Encountered unexpected Throwable", throwable);
		}
	}

	private String serialize(final Object o) throws SerializationFailedException {
		final SerializationFailedException serializationFailedException;
		try {
			logging.trace("[SendingService{" + connectionID.get() + "}] Trying to use mainSerializationAdapter for " + o + " .. ");
			return mainSerializationAdapter.get(o);
		} catch (SerializationFailedException ex) {
			logging.trace("[SendingService{" + connectionID.get() + "}] Failed to use mainSerializationAdapter for " + o + " .. Reaching for fallback ..");
			serializationFailedException = new SerializationFailedException(ex);
			for (final SerializationAdapter<Object, String> adapter : fallBackSerialization) {
				try {
					logging.trace("[SendingService{" + connectionID.get() + "}] Trying to use: " + adapter + " ..");
					return adapter.get(o);
				} catch (SerializationFailedException e) {
					logging.trace("[SendingService{" + connectionID.get() + "}] Fallback serialization " + adapter + " failed .. Trying next one");
					serializationFailedException.addSuppressed(e);
				}
			}
		}
		logging.warn("[SendingService{" + connectionID.get() + "}] No fallback serialization found! Failed to serialize " + o + "!");
		throw new SerializationFailedException(serializationFailedException);
	}

	private String encrypt(final String s) {
		return encryptionAdapter.get(s);
	}

	private void triggerCallbacks(final Object o) {
		final List<Callback<Object>> temp;
		synchronized (callbacks) {
			temp = new ArrayList<>(callbacks);
		}
		temp.stream()
				.filter(callBack -> callBack.isAcceptable(o))
				.forEachOrdered(callBack -> callBack.accept(o));

		threadPool.submit(this::tryClearCallBacks);
	}

	private void deleteCallBack(final Callback<Object> callback) {
		logging.debug("[SendingService{" + connectionID.get() + "}] Removing " + callback + " from SendingService ..");
		synchronized (callbacks) {
			callbacks.remove(callback);
		}
	}

	private void tryClearCallBacks() {
		final List<Callback<Object>> removable = new ArrayList<>();

		synchronized (callbacks) {
			callbacks.stream()
					.filter(Callback::isRemovable)
					.forEachOrdered(callBack -> {
						logging.debug("[SendingService{" + connectionID.get() + "}] Marking " + callBack + " as to be removed ..");
						removable.add(callBack);
					});
		}

		removable.forEach(this::deleteCallBack);
	}

	@Override
	public void addSendDoneCallback(final Callback<Object> callback) {
		synchronized (callbacks) {
			callbacks.add(callback);
		}
	}

	@Override
	public void overrideSendingQueue(final BlockingQueue<Object> linkedBlockingQueue) {
		NetCom2Utils.assertNotNull(linkedBlockingQueue);
		logging.warn("[SendingService{" + connectionID.get() + "}] Overriding the sending-hook should be used with caution!");
		this.toSend = linkedBlockingQueue;
	}

	@Override
	public void setup(final OutputStream outputStream, final BlockingQueue<Object> toSendFrom) {
		NetCom2Utils.assertNotNull(outputStream, toSendFrom);
		this.printWriter = new PrintWriter(outputStream);
		this.toSend = toSendFrom;
		setup = true;
		logging.debug("[SendingService{" + connectionID.get() + "}] DefaultSendingService is now setup!");
	}

	@Override
	public Awaiting started() {
		return synchronize;
	}

	@Override
	public void softStop() {
		running = false;
		// This is done, to possibly awake the waiting SendingQueue earlier
		// It may have no effect. This is to be evaluated
		toSend.notifyAll();
	}

	@Override
	public boolean running() {
		return running;
	}
}
