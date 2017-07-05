package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.exceptions.SerializationFailedException;

import java.util.ArrayList;
import java.util.List;

public interface SerializationAdapter<F, T> {

	static SerializationAdapter<Object, String> getDefaultJavaDeSerialization() {
		return new JavaSerializationAdapter();
	}

	static List<SerializationAdapter<Object, String>> getDefaultFallback() {
		List<SerializationAdapter<Object, String>> toReturn = new ArrayList<>();
		toReturn.add(new PingSerializationAdapter());
		return toReturn;
	}

	T get(F f) throws SerializationFailedException;
}
