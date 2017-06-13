package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.exceptions.SerializationFailedException;
import de.thorbenkuck.netcom2.logging.NetComLogging;
import de.thorbenkuck.netcom2.network.interfaces.Logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Base64;

public class JavaSerializationAdapter implements SerializationAdapter<Object, String> {

	private final Logging logging = NetComLogging.getLogging();

	@Override
	public String get(Object o) throws SerializationFailedException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			logging.trace("Creating ObjectOutputStream..");
			oos = new ObjectOutputStream(baos);
			logging.trace("Writing object..");
			oos.writeObject(o);
			logging.trace("Done!");
		} catch (IOException e) {
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
		String toReturn = Base64.getEncoder().encodeToString(baos.toByteArray());
		logging.trace("Encoded " + o + " to " + toReturn);
		return toReturn;
	}

	@Override
	public String toString() {
		return "JavaSerializationAdapter{Default SerializationAdapter requiring java.io.Serializable}";
	}

}
