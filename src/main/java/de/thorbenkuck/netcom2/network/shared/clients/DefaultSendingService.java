package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.exceptions.SerializationFailedException;
import de.thorbenkuck.netcom2.logging.NetComLogging;
import de.thorbenkuck.netcom2.network.client.DefaultSynchronize;
import de.thorbenkuck.netcom2.network.client.EncryptionAdapter;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.interfaces.SendingService;
import de.thorbenkuck.netcom2.network.shared.Awaiting;
import de.thorbenkuck.netcom2.network.shared.Synchronize;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

class DefaultSendingService implements SendingService {

	private final SerializationAdapter<Object, String> mainSerializationAdapter;
	private final Set<SerializationAdapter<Object, String>> fallBackSerialization;
	private final EncryptionAdapter encryptionAdapter;
	private final Logging logging = new NetComLogging();
	private final Synchronize synchronize = new DefaultSynchronize(1);
	private PrintWriter printWriter;
	private LinkedBlockingQueue<Object> toSend;
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
				logging.debug("Sending " + o + " ..");
				logging.trace("Serializing " + o + " ..");
				String toSend = serialize(o);
				logging.trace("Writing: " + toSend + " ..");
				printWriter.println(encryptionAdapter.get(toSend));
				printWriter.flush();
				logging.trace("Successfully wrote " + toSend + "!");
			} catch (InterruptedException e) {
				logging.warn("Interrupted while waiting for a new Object to send");
				logging.catching(e);
			} catch (SerializationFailedException e) {
				logging.error("Failed to Serialize!", e);
			} catch (Throwable throwable) {
				logging.error("Encountered unexpected Throwable", throwable);
			}
		}
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

	@Override
	public void softStop() {
		running = false;
	}

	@Override
	public boolean running() {
		return running;
	}

	@Override
	public void overrideSendingQueue(LinkedBlockingQueue<Object> linkedBlockingQueue) {
		logging.warn("Overriding the sending-hook should be used with caution!");
		this.toSend = linkedBlockingQueue;
	}

	@Override
	public void setup(OutputStream outputStream, LinkedBlockingQueue<Object> toSendFrom) {
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
