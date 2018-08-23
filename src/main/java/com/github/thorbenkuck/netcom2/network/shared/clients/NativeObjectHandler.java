package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.pipe.Pipeline;
import com.github.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;
import com.github.thorbenkuck.netcom2.exceptions.SerializationFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.DeSerializationAdapter;
import com.github.thorbenkuck.netcom2.network.shared.DecryptionAdapter;
import com.github.thorbenkuck.netcom2.network.shared.EncryptionAdapter;
import com.github.thorbenkuck.netcom2.network.shared.SerializationAdapter;

import java.util.ArrayList;
import java.util.List;

class NativeObjectHandler implements ObjectHandler {

	private final Value<SerializationAdapter> mainSerializationAdapter = Value.synchronize(new JavaSerializationAdapter());
	private final Value<DeSerializationAdapter> mainDeserializationAdapter = Value.synchronize(new JavaDeserializationAdapter());
	private final List<SerializationAdapter> fallbackSerializationList = new ArrayList<>();
	private final List<DeSerializationAdapter> fallbackDeserializationList = new ArrayList<>();
	private final Pipeline<String> decryptionPipeline = Pipeline.unifiedCreation();
	private final Pipeline<String> encryptionPipeline = Pipeline.unifiedCreation();
	private final Logging logging = Logging.unified();

	NativeObjectHandler() {
		logging.instantiated(this);
	}

	@Override
	public void addFallbackSerialization(SerializationAdapter serializationAdapter) {
		synchronized (fallbackSerializationList) {
			fallbackSerializationList.add(serializationAdapter);
		}
	}

	@Override
	public void addFallbackDeserialization(DeSerializationAdapter deSerializationAdapter) {
		synchronized (fallbackDeserializationList) {
			fallbackDeserializationList.add(deSerializationAdapter);
		}
	}

	@Override
	public void setMainSerialization(SerializationAdapter serializationAdapter) {
		mainSerializationAdapter.set(serializationAdapter);
	}

	@Override
	public void setMainDeserialization(DeSerializationAdapter deSerializationAdapter) {
		mainDeserializationAdapter.set(deSerializationAdapter);
	}

	@Override
	public void addEncryptionAdapter(EncryptionAdapter encryptionAdapter) {
		encryptionPipeline.add(encryptionAdapter);
	}

	@Override
	public void addDecryptionAdapter(DecryptionAdapter decryptionAdapter) {
		decryptionPipeline.add(decryptionAdapter);
	}

	private String mainSerialize(Object object) throws SerializationFailedException {
		if (mainSerializationAdapter.isEmpty()) {
			throw new SerializationFailedException("No main SerializationAdapter present!");
		}
		SerializationAdapter mainSerialization = mainSerializationAdapter.get();
		return mainSerialization.apply(object);
	}

	private String fallbackSerialize(Object object, SerializationFailedException s) throws SerializationFailedException {
		final List<SerializationAdapter> fallbackCopy;

		synchronized (fallbackSerializationList) {
			fallbackCopy = new ArrayList<>(fallbackSerializationList);
		}

		for (SerializationAdapter adapter : fallbackCopy) {
			try {
				return adapter.apply(object);
			} catch (SerializationFailedException e) {
				s.addSuppressed(e);
			}
		}

		throw s;
	}

	private String serialize(Object object) throws SerializationFailedException {
		try {
			return mainSerialize(object);
		} catch (SerializationFailedException e) {
			return fallbackSerialize(object, e);
		}
	}

	private Object mainDeserialize(String string) throws DeSerializationFailedException {
		if (mainSerializationAdapter.isEmpty()) {
			throw new DeSerializationFailedException("No main SerializationAdapter present!");
		}
		DeSerializationAdapter mainSerialization = mainDeserializationAdapter.get();
		return mainSerialization.apply(string);
	}

	private Object fallbackDeserialize(String string, DeSerializationFailedException s) throws DeSerializationFailedException {
		final List<DeSerializationAdapter> fallbackCopy;

		synchronized (fallbackDeserializationList) {
			fallbackCopy = new ArrayList<>(fallbackDeserializationList);
		}

		for (DeSerializationAdapter adapter : fallbackCopy) {
			try {
				return adapter.apply(string);
			} catch (DeSerializationFailedException e) {
				s.addSuppressed(e);
			}
		}

		throw s;
	}

	private Object deserialize(String string) throws DeSerializationFailedException {
		try {
			return mainDeserialize(string);
		} catch (DeSerializationFailedException e) {
			return fallbackDeserialize(string, e);
		}
	}

	@Override
	public String convert(Object object) throws SerializationFailedException {
		String serialized = serialize(object);
		String encrypted = encryptionPipeline.apply(serialized);
		logging.trace("Calculated encrypted result. Returning..");
		return encrypted;
	}

	@Override
	public Object convert(String string) throws DeSerializationFailedException {
		String decrypted = decryptionPipeline.apply(string);
		Object deSerialized = deserialize(decrypted);
		logging.trace("Calculated received object");
		logging.debug("Converted to " + deSerialized.getClass());
		return deSerialized;
	}
}
