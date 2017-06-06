package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;

public interface DeSerializationAdapter<F, T> {
	static DeSerializationAdapter<String, Object> getDefault() {
		return new JavaDeSerializationAdapter();
	}

	T get(F f) throws DeSerializationFailedException;
}
