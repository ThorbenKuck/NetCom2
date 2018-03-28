package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.annotations.Tested;
import com.github.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Base64;

/**
 * This Adapter utilizes the Java-Serialization to deserialize an String into an Object.
 *
 * @version 1.0
 * @since 1.0
 */
@Synchronized
@Tested(responsibleTest = "com.github.thorbenkuck.netcom2.network.shared.clients.JavaDeSerializationAdapterTest")
public class JavaDeSerializationAdapter implements DeSerializationAdapter<String, Object> {

	private final Logging logging = Logging.unified();

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "JavaDeSerializationAdapter{Default DeSerializationAdapter requiring java.io.Serializable}";
	}
}
