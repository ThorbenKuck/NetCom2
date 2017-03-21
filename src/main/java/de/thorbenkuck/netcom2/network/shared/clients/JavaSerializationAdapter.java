package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.exceptions.SerializationFailedException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Base64;

public class JavaSerializationAdapter implements SerializationAdapter<Object, String> {
	@Override
	public String get(Object o) throws SerializationFailedException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(o);
			oos.close();
		} catch (IOException e) {
			throw new SerializationFailedException(e);
		}
		return Base64.getEncoder().encodeToString(baos.toByteArray());
	}
}
