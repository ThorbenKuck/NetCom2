package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.exceptions.SerializationFailedException;
import de.thorbenkuck.netcom2.logging.NetComLogging;
import de.thorbenkuck.netcom2.network.client.DefaultSynchronize;
import de.thorbenkuck.netcom2.network.client.EncryptionAdapter;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.interfaces.SendingService;
import de.thorbenkuck.netcom2.network.shared.Awaiting;
import de.thorbenkuck.netcom2.network.shared.CallBack;
import de.thorbenkuck.netcom2.network.shared.Synchronize;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class DefaultSendingService implements SendingService {

	private final SerializationAdapter<Object, String> mainSerializationAdapter;
	private final Set<SerializationAdapter<Object, String>> fallBackSerialization;
	private final EncryptionAdapter encryptionAdapter;
	private final Logging logging = new NetComLogging();
	private final Synchronize synchronize = new DefaultSynchronize(1);
	private final ExecutorService threadPool = Executors.newCachedThreadPool();
	private final List<CallBack<Object>> callBacks = new ArrayList<>();
	private PrintWriter printWriter;
	private BlockingQueue<Object> toSend;
	private boolean running = false;
	private boolean setup = false;

	DefaultSendingService(SerializationAdapter<Object, String> mainSerializationAdapter,
						  Set<SerializationAdapter<Object, String>> fallBackSerialization,
						  EncryptionAdapter encryptionAdapter) {
		this.mainSerializationAdapter = mainSerializationAdapter;
		this.fallBackSerialization = fallBackSerialization;
		this.encryptionAdapter = encryptionAdapter;
	}

	@Override
	public void run() {
		if (! setup) {
			throw new Error("Setup required before run!");
		}
		running = true;
		logging.debug("Started Sending Service");
		synchronize.goOn();
		while (running()) {
			try {
				Object o = toSend.take();
				// Take it first, then beforeSend it in another thread!
				threadPool.submit(() -> send(o));
			} catch (InterruptedException e) {
				if(running) {
					logging.warn("Interrupted while waiting for a new Object to beforeSend");
					logging.catching(e);
				}
			}
		}
		logging.info("SendingService stopped!");
	}

	private void send(Object o) {
		try {
			logging.debug("Sending " + o + " ..");
			logging.trace("Serializing " + o + " ..");
			String toSend = serialize(o);
			logging.trace("Encrypting " + toSend + " ..");
			toSend = encrypt(toSend);
			logging.trace("Writing: " + toSend + " ..");
			printWriter.println(encryptionAdapter.get(toSend));
			printWriter.flush();
			logging.trace("Successfully wrote " + toSend + "!");
			// help the GC
			toSend = null;
			logging.trace("Accepting CallBacks ..");
			triggerCallbacks(o);
		} catch (SerializationFailedException e) {
			logging.error("Failed to Serialize!", e);
		} catch (Throwable throwable) {
			logging.error("Encountered unexpected Throwable", throwable);
		}
	}

	private void triggerCallbacks(Object o) {
		List<CallBack<Object>> temp;
		synchronized(callBacks) {
			temp = new ArrayList<>(callBacks);
		}
		temp.stream()
				.filter(callBack -> callBack.isAcceptable(o))
				.forEachOrdered(callBack -> callBack.accept(o));

		threadPool.submit(this::tryClearCallBacks);
	}

	private void deleteCallBack(CallBack<Object> callBack) {
		logging.debug("Removing " + callBack + " from SendingService ..");
		synchronized(callBacks) {
			callBacks.remove(callBack);
		}
	}

	private void tryClearCallBacks() {
		List<CallBack<Object>> removable = new ArrayList<>();

		synchronized(callBacks) {
			callBacks.stream()
					.filter(CallBack::isRemovable)
					.forEachOrdered(callBack -> {
						logging.debug("Marking " + callBack + " as to be removed ..");
						removable.add(callBack);
					});
		}

		removable.forEach(this::deleteCallBack);
	}

	private String serialize(Object o) throws SerializationFailedException {
		SerializationFailedException serializationFailedException;
		try {
			logging.trace("Trying to use mainSerializationAdapter for " + o + " .. ");
			return mainSerializationAdapter.get(o);
		} catch (SerializationFailedException ex) {
			logging.trace("Failed to use mainSerializationAdapter for " + o + " .. Reaching for fallback ..");
			serializationFailedException = new SerializationFailedException(ex);
			for (SerializationAdapter<Object, String> adapter : fallBackSerialization) {
				try {
					logging.trace("Trying to use: " + adapter + " ..");
					return adapter.get(o);
				} catch (SerializationFailedException e) {
					logging.trace("Fallback serialization " + adapter + " failed .. Trying next one");
					serializationFailedException.addSuppressed(e);
				}
			}
		}
		logging.warn("No fallback serialization found! Failed to serialize " + o + "!");
		throw new SerializationFailedException(serializationFailedException);
	}

	private String encrypt(String s) {
		return encryptionAdapter.get(s);
	}

	@Override
	public void addSendDoneCallback(CallBack<Object> callback) {
		synchronized(callBacks) {
			callBacks.add(callback);
		}
	}

	@Override
	public void softStop() {
		running = false;
	}

	@Override
	public boolean running() {
		return running;
	}

	@Override
	public void overrideSendingQueue(BlockingQueue<Object> linkedBlockingQueue) {
		Objects.requireNonNull(linkedBlockingQueue);
		logging.warn("Overriding the sending-hook should be used with caution!");
		this.toSend = linkedBlockingQueue;
	}

	@Override
	public void setup(OutputStream outputStream, BlockingQueue<Object> toSendFrom) {
		Objects.requireNonNull(outputStream);
		Objects.requireNonNull(toSendFrom);
		this.printWriter = new PrintWriter(outputStream);
		this.toSend = toSendFrom;
		setup = true;
		logging.debug("DefaultSendingService is now setup!");
	}

	@Override
	public Awaiting started() {
		return synchronize;
	}
}
