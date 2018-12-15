package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;
import com.github.thorbenkuck.netcom2.network.shared.DeSerializationAdapter;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Base64;

public class JavaDeserializationAdapter implements DeSerializationAdapter {

	@Override
	public Object apply(String string) throws DeSerializationFailedException {
		final Object o;
		byte[] data = Base64.getDecoder().decode(string.getBytes());

		try (final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
			o = ois.readObject();
		} catch (final Throwable e) {
			throw new DeSerializationFailedException("Error while reading the given serialized Object.. " +
					"\nGiven serialized Object: " + string, e);
		}

		// TODO Evaluate whether or not this could happen
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
