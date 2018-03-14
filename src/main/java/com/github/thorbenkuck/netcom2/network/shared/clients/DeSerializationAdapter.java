package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;

import java.util.ArrayList;
import java.util.List;

public interface DeSerializationAdapter<F, T> {

	static DeSerializationAdapter<String, Object> getDefaultJavaSerialization() {
		return new JavaDeSerializationAdapter();
	}

	static List<DeSerializationAdapter<String, Object>> getDefaultFallback() {
		List<DeSerializationAdapter<String, Object>> toReturn = new ArrayList<>();
		toReturn.add(new PingDeSerializationAdapter());
		return toReturn;
	}

	T get(final F f) throws DeSerializationFailedException;
}
