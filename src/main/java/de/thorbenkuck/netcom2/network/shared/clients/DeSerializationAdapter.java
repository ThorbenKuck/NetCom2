package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;

public interface DeSerializationAdapter<F, T> {
	T get(F f) throws DeSerializationFailedException;
}
