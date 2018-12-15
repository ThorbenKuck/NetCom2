package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.exceptions.SerializationFailedException;
import com.github.thorbenkuck.netcom2.network.shared.SerializationAdapter;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Base64;

public class JavaSerializationAdapter implements SerializationAdapter {

	@Override
	public String apply(final Object o) throws SerializationFailedException {
		NetCom2Utils.parameterNotNull(o);
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		try (ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutputStream)) {
			oos.writeObject(o);
		} catch (final IOException e) {
			throw new SerializationFailedException(e);
		}

		return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "JavaSerializationAdapter{Default SerializationAdapter requiring java.io.Serializable}";
	}
}
