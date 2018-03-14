package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.exceptions.SerializationFailedException;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Base64;

public class JavaSerializationAdapter implements SerializationAdapter<Object, String> {

	private final Logging logging = Logging.unified();

	@Asynchronous
	@Override
	public String get(final Object o) throws SerializationFailedException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			logging.trace("Creating ObjectOutputStream..");
			oos = new ObjectOutputStream(baos);
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
					e.printStackTrace();
				}
			}
			try {
				baos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		final String toReturn = Base64.getEncoder().encodeToString(baos.toByteArray());
		logging.trace("Encoded " + o + " to " + toReturn);
		return toReturn;
	}

	@Override
	public String toString() {
		return "JavaSerializationAdapter{Default SerializationAdapter requiring java.io.Serializable}";
	}

}
