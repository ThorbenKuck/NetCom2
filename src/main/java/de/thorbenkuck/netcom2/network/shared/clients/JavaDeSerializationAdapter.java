package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Base64;

public class JavaDeSerializationAdapter implements DeSerializationAdapter<String, Object> {
	@Override
	public Object get(String s) throws DeSerializationFailedException {
		byte[] data = Base64.getDecoder().decode(s);
		ObjectInputStream ois;
		Object o;
		try {
			ois = new ObjectInputStream(
					new ByteArrayInputStream(data));
			o = ois.readObject();
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			throw new DeSerializationFailedException("Error while reading the given serialized NetComCommand .. " +
					"\nGiven serialized NetComCommand: " + s, e);
		}

		if (o == null) {
			throw new DeSerializationFailedException("Error while reading the given serialized NetComCommand .. " +
					"\nGiven serialized NetComCommand: " + s);
		}
		return o;
	}

	@Override
	public String toString() {
		return "JavaDeSerializationAdapter{Default DeSerializationAdapter requiring java.io.Serializable}";
	}
}
