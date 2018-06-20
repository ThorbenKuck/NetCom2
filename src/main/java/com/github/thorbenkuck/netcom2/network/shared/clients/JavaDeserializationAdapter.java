package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.DeSerializationAdapter;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Base64;

public class JavaDeserializationAdapter implements DeSerializationAdapter {

	private final Logging logging = Logging.unified();

	@Override
	public Object apply(String string) throws DeSerializationFailedException {
		final Object o;
		try {
			logging.trace("DeSerialization of " + string);
			byte[] data = Base64.getDecoder().decode(string.getBytes());
			logging.trace("Decoded bytes " + Arrays.toString(data));
			final ObjectInputStream ois = new ObjectInputStream(
					new ByteArrayInputStream(data));
			logging.trace("Reading Object..");
			o = ois.readObject();
			logging.trace("Decoded Object: " + o);
			ois.close();
		} catch (final Throwable e) {
			throw new DeSerializationFailedException("Error while reading the given serialized Object.. " +
					"\nGiven serialized Object: " + string, e);
		}

		if (o == null) {
			throw new DeSerializationFailedException("Error while reading the given serialized Object .. " +
					"\nGiven serialized Object: " + string);
		}
		return o;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "JavaDeSerializationAdapter{Default DeSerializationAdapter requiring java.io.Serializable}";
	}
}
