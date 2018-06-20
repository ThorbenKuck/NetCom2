package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;

@FunctionalInterface
public interface DeSerializationAdapter {

	Object apply(String string) throws DeSerializationFailedException;

	@Deprecated
	default Object get(String string) throws DeSerializationFailedException {
		return apply(string);
	}

}
