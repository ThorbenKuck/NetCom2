package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;
import de.thorbenkuck.netcom2.logging.NetComLogging;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.comm.model.Ping;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Base64;

public class JavaDeSerializationAdapter implements DeSerializationAdapter<String, Object> {

	private final Logging logging = NetComLogging.getLogging();

	@Override
	public Object get(String s) throws DeSerializationFailedException {
		if (s.startsWith("Ping")) {
			return deSerializePing(s);
		}
		Object o;
		try {
			logging.trace("DeSerialization of " + s);
			byte[] data = Base64.getDecoder().decode(s.getBytes());
			logging.trace("Decoded bytes " + Arrays.toString(data));
			ObjectInputStream ois = new ObjectInputStream(
					new ByteArrayInputStream(data));
			logging.trace("Reading Object..");
			o = ois.readObject();
			logging.trace("Decoded Object: " + o);
			ois.close();
		} catch (Throwable e) {
			throw new DeSerializationFailedException("Error while reading the given serialized Object.. " +
					"\nGiven serialized Object: " + s, e);
		}

		if (o == null) {
			throw new DeSerializationFailedException("Error while reading the given serialized Object .. " +
					"\nGiven serialized Object: " + s);
		}
		return o;
	}

	private Object deSerializePing(String s) {
		return new Ping(ClientID.fromString(s.split("\\|")[1]));
	}

	@Override
	public String toString() {
		return "JavaDeSerializationAdapter{Default DeSerializationAdapter requiring java.io.Serializable}";
	}
}
