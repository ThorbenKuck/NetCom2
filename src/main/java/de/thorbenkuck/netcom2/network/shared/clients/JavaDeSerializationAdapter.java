package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;
import de.thorbenkuck.netcom2.logging.LoggingUtil;
import de.thorbenkuck.netcom2.network.interfaces.Logging;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Base64;

public class JavaDeSerializationAdapter implements DeSerializationAdapter<String, Object> {

	private final Logging logging = LoggingUtil.getLogging();

	@Override
	public Object get(String s) throws DeSerializationFailedException {
		logging.trace("Entered JavaDeSerializationAdapter#get");
		byte[] data = Base64.getDecoder().decode(s);
		ObjectInputStream ois;
		Object o;
		try {
			ois = new ObjectInputStream(
					new ByteArrayInputStream(data));
			o = ois.readObject();
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			logging.trace("Leaving JavaDeSerializationAdapter#get");
			throw new DeSerializationFailedException("Error while reading the given serialized NetComCommand .. " +
					"\nGiven serialized NetComCommand: " + s, e);
		}

		if (o == null) {
			logging.trace("Leaving JavaDeSerializationAdapter#get");
			throw new DeSerializationFailedException("Error while reading the given serialized NetComCommand .. " +
					"\nGiven serialized NetComCommand: " + s);
		}
		logging.trace("Entered JavaDeSerializationAdapter#get");
		return o;
	}

	@Override
	public String toString() {
		return "JavaDeSerializationAdapter{Default DeSerializationAdapter requiring java.io.Serializable}";
	}
}
