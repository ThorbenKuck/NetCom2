package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import com.github.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;
import com.github.thorbenkuck.netcom2.exceptions.SerializationFailedException;
import com.github.thorbenkuck.netcom2.network.interfaces.DecryptionAdapter;
import com.github.thorbenkuck.netcom2.network.interfaces.EncryptionAdapter;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.clients.DeSerializationAdapter;
import com.github.thorbenkuck.netcom2.network.shared.clients.SerializationAdapter;

import java.util.Set;
import java.util.function.Supplier;

class ObjectHandler {

	private final Supplier<SerializationAdapter<Object, String>> mainSerializationAdapterSupplier;
	private final Supplier<Set<SerializationAdapter<Object, String>>> fallbackSerializationAdapterSupplier;
	private final Supplier<DeSerializationAdapter<String, Object>> mainDeSerializationAdapterSupplier;
	private final Supplier<Set<DeSerializationAdapter<String, Object>>> fallbackDeSerializationAdapterSupplier;
	private final Supplier<EncryptionAdapter> encryptionAdapterSupplier;
	private final Supplier<DecryptionAdapter> decryptionAdapterSupplier;
	private final Logging logging = Logging.unified();

	ObjectHandler(Supplier<SerializationAdapter<Object, String>> mainSerializationAdapterSupplier,
	              Supplier<Set<SerializationAdapter<Object, String>>> fallbackSerializationAdapterSupplier,
	              Supplier<DeSerializationAdapter<String, Object>> mainDeSerializationAdapterSupplier,
	              Supplier<Set<DeSerializationAdapter<String, Object>>> fallbackDeSerializationAdapterSupplier,
	              Supplier<EncryptionAdapter> encryptionAdapterSupplier, Supplier<DecryptionAdapter> decryptionAdapterSupplier) {
		this.mainSerializationAdapterSupplier = mainSerializationAdapterSupplier;
		this.fallbackSerializationAdapterSupplier = fallbackSerializationAdapterSupplier;
		this.mainDeSerializationAdapterSupplier = mainDeSerializationAdapterSupplier;
		this.fallbackDeSerializationAdapterSupplier = fallbackDeSerializationAdapterSupplier;
		this.encryptionAdapterSupplier = encryptionAdapterSupplier;
		this.decryptionAdapterSupplier = decryptionAdapterSupplier;
	}

	public String serialize(Object o) throws SerializationFailedException {
		final SerializationFailedException serializationFailedException;
		try {
			return encrypt(mainSerializationAdapterSupplier.get().get(o));
		} catch (SerializationFailedException ex) {
			logging.trace("Failed to use mainSerializationAdapter for " + o + " .. Reaching for fallback ..");
			serializationFailedException = new SerializationFailedException(ex);
			for (final SerializationAdapter<Object, String> adapter : fallbackSerializationAdapterSupplier.get()) {
				try {
					logging.trace("Trying to use: " + adapter + " ..");
					return encrypt(adapter.get(o));
				} catch (SerializationFailedException e) {
					logging.trace("Fallback serialization " + adapter + " failed .. Trying next one");
					serializationFailedException.addSuppressed(e);
				}
			}
		}
		logging.warn("No fallback serialization found! Failed to serialize " + o + "!");
		throw new SerializationFailedException(serializationFailedException);
	}

	public Object deserialize(String s) throws DeSerializationFailedException {
		final String toDeserialize = decrypt(s);
		final DeSerializationFailedException deSerializationFailedException;
		try {
			return mainDeSerializationAdapterSupplier.get().get(toDeserialize);
		} catch (final DeSerializationFailedException ex) {
			logging.warn("MainSerializationAdapter failed.");
			deSerializationFailedException = new DeSerializationFailedException(ex);
			for (final DeSerializationAdapter<String, Object> adapter : fallbackDeSerializationAdapterSupplier.get()) {
				try {
					return adapter.get(toDeserialize);
				} catch (final DeSerializationFailedException e) {
					deSerializationFailedException.addSuppressed(e);
				}
			}
		}
		throw new DeSerializationFailedException(deSerializationFailedException);
	}

	private String decrypt(String s) {
		return decryptionAdapterSupplier.get().get(s);
	}

	private String encrypt(String s) {
		return encryptionAdapterSupplier.get().get(s);
	}
}
