package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Base64;

public class JavaDeSerializationAdapter implements DeSerializationAdapter<String, Object> {

	private final Logging logging = Logging.unified();

	@Asynchronous
	@Override
	public Object get(final String s) throws DeSerializationFailedException {
		final Object o;
		try {
			logging.trace("DeSerialization of " + s);
			byte[] data = Base64.getDecoder().decode(s.getBytes());
			logging.trace("Decoded bytes " + Arrays.toString(data));
			final ObjectInputStream ois = new ObjectInputStream(
					new ByteArrayInputStream(data));
			logging.trace("Reading Object..");
			o = ois.readObject();
			logging.trace("Decoded Object: " + o);
			ois.close();
		} catch (final Throwable e) {
			throw new DeSerializationFailedException("Error while reading the given serialized Object.. " +
					"\nGiven serialized Object: " + s, e);
		}

		if (o == null) {
			throw new DeSerializationFailedException("Error while reading the given serialized Object .. " +
					"\nGiven serialized Object: " + s);
		}
		return o;
	}

	@Override
	public String toString() {
		return "JavaDeSerializationAdapter{Default DeSerializationAdapter requiring java.io.Serializable}";
	}
}
