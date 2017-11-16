package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.exceptions.SerializationFailedException;

import java.util.ArrayList;
import java.util.List;

public interface SerializationAdapter<F, T> {

	static SerializationAdapter<Object, String> getDefaultJavaDeSerialization() {
		return new JavaSerializationAdapter();
	}

	static List<SerializationAdapter<Object, String>> getDefaultFallback() {
		final List<SerializationAdapter<Object, String>> toReturn = new ArrayList<>();
		toReturn.add(new PingSerializationAdapter());
		return toReturn;
	}

	T get(final F f) throws SerializationFailedException;
}
