package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.netcom2.exceptions.SerializationFailedException;

@FunctionalInterface
public interface SerializationAdapter {

	String apply(Object object) throws SerializationFailedException;

	@Deprecated
	default String get(Object object) throws SerializationFailedException {
		return apply(object);
	}

}
