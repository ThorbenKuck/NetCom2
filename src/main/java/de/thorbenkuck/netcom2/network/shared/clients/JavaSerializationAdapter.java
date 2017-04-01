package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.exceptions.SerializationFailedException;
import de.thorbenkuck.netcom2.logging.LoggingUtil;
import de.thorbenkuck.netcom2.network.interfaces.Logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Base64;

public class JavaSerializationAdapter implements SerializationAdapter<Object, String> {

	private final Logging logging = LoggingUtil.getLogging();

	@Override
	public String get(Object o) throws SerializationFailedException {
		logging.trace("Entered JavaSerializationAdapter#get");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(o);
			oos.close();
		} catch (IOException e) {
			logging.trace("Leaving JavaSerializationAdapter#get");
			throw new SerializationFailedException(e);
		}
		logging.trace("Leaving JavaSerializationAdapter#get");
		return Base64.getEncoder().encodeToString(baos.toByteArray());
	}

	@Override
	public String toString() {
		return "JavaSerializationAdapter{Default SerializationAdapter requiring java.io.Serializable}";
	}

}
