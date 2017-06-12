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
		synchronize.goOn();
		logging.debug("Started Sending Service");
		while (running()) {
			try {
				Object o = toSend.take();
				logging.debug("Sending " + o + " ..");
				String toSend = serialize(o);
				logging.trace("Sending: " + toSend);
				printWriter.println(encryptionAdapter.get(toSend));
				printWriter.flush();
				logging.trace("Done with: " + toSend);
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

	@Override
	public void overrideSendingQueue(LinkedBlockingQueue<Object> linkedBlockingQueue) {
		this.toSend = linkedBlockingQueue;
	}

	@Override
	public void setup(OutputStream outputStream, LinkedBlockingQueue<Object> toSendFrom) {
		setup = true;
		this.printWriter = new PrintWriter(outputStream);
		this.toSend = toSendFrom;
	}

	@Override
	public Awaiting started() {
		return synchronize;
	}
}
