package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.exceptions.SerializationFailedException;

public interface SerializationAdapter<F, T> {
	T get(F f) throws SerializationFailedException;
}
