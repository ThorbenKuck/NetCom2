package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.annotations.Tested;
import com.github.thorbenkuck.netcom2.exceptions.SerializationFailedException;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Base64;

/**
 * This SerializationAdapter utilizes the Java-serialization, to serialize an Object into an String
 *
 * @version 1.0
 * @since 1.0
 */
@Synchronized
@Tested(responsibleTest = "com.github.thorbenkuck.netcom2.network.shared.clients.JavaSerializationAdapterTest")
public class JavaSerializationAdapter implements SerializationAdapter<Object, String> {

	private final Logging logging = Logging.unified();

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
	@Override
	public String get(final Object o) throws SerializationFailedException {
		NetCom2Utils.parameterNotNull(o);
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
					logging.catching(e);
				}
			}
			try {
				baos.close();
			} catch (IOException e) {
				logging.catching(e);
			}
		}
		final String toReturn = Base64.getEncoder().encodeToString(baos.toByteArray());
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
