package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.exceptions.SerializationFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.SerializationAdapter;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Base64;

public class JavaSerializationAdapter implements SerializationAdapter {

	private final Logging logging = Logging.unified();

	@Override
	public String apply(final Object o) throws SerializationFailedException {
		NetCom2Utils.parameterNotNull(o);
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			logging.trace("Creating ObjectOutputStream..");
			oos = new ObjectOutputStream(byteArrayOutputStream);
			logging.trace("Writing object..");
			oos.writeObject(o);
			logging.trace("Done!");
		} catch (final IOException e) {
			throw new SerializationFailedException(e);
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					logging.catching(e);
				}
			}
			try {
				byteArrayOutputStream.close();
			} catch (IOException e) {
				logging.catching(e);
			}
		}
		final String toReturn = Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
		logging.trace("Encoded " + o + " to " + toReturn);
		return toReturn;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "JavaSerializationAdapter{Default SerializationAdapter requiring java.io.Serializable}";
	}
}
