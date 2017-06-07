package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.exceptions.SerializationFailedException;
import de.thorbenkuck.netcom2.logging.NetComLogging;
import de.thorbenkuck.netcom2.network.client.EncryptionAdapter;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.interfaces.SendingService;

import java.io.PrintWriter;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

class DefaultSendingService implements SendingService {

	private final LinkedBlockingQueue<Object> toSend;
	private final SerializationAdapter<Object, String> mainSerializationAdapter;
	private final Set<SerializationAdapter<Object, String>> fallBackSerialization;
	private final PrintWriter printWriter;
	private final EncryptionAdapter encryptionAdapter;
	private final Logging logging = new NetComLogging();
	private boolean running = false;

	DefaultSendingService(LinkedBlockingQueue<Object> toSend,
						  SerializationAdapter<Object, String> mainSerializationAdapter,
						  Set<SerializationAdapter<Object, String>> fallBackSerialization, PrintWriter printWriter,
						  EncryptionAdapter encryptionAdapter) {
		this.toSend = toSend;
		this.mainSerializationAdapter = mainSerializationAdapter;
		this.fallBackSerialization = fallBackSerialization;
		this.printWriter = printWriter;
		this.encryptionAdapter = encryptionAdapter;
	}

	@Override
	public void run() {
		logging.trace("Started Sending Service!");
		running = true;
		while (running()) {
			try {
				Object o = toSend.take();
				logging.debug("Sending " + o + " ..");
				String toSend = serialize(o);
				printWriter.println(encryptionAdapter.get(toSend));
				printWriter.flush();
			} catch (InterruptedException e) {
				logging.catching(e);
			} catch (SerializationFailedException e) {
				logging.warn("Failed to Serialize!");
				logging.catching(e);
			}
		}
	}

	private String serialize(Object o) throws SerializationFailedException {
		SerializationFailedException serializationFailedException;
		try {
			return mainSerializationAdapter.get(o);
		} catch (SerializationFailedException ex) {
			serializationFailedException = new SerializationFailedException(ex);
			for (SerializationAdapter<Object, String> adapter : fallBackSerialization) {
				try {
					return adapter.get(o);
				} catch (SerializationFailedException e) {
					serializationFailedException.addSuppressed(e);
				}
			}
		}
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
}
