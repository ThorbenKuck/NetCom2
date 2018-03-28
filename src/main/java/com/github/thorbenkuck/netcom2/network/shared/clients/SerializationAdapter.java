package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.exceptions.SerializationFailedException;

import java.util.ArrayList;
import java.util.List;

/**
 * This Adapter will serialize a <code>F</code> into a <code>T</code>.
 *
 * @param <F> the input-type of this adapter.
 * @param <T> the output-type of this adapter.
 * @version 1.0
 * @since 1.0
 */
public interface SerializationAdapter<F, T> {

	/**
	 * Creates the default SerializationAdapter, utilizing the Java-Serialization.
	 *
	 * @return a new Instance.
	 */
	static SerializationAdapter<Object, String> getDefaultJavaDeSerialization() {
		return new JavaSerializationAdapter();
	}

	/**
	 * Creates the default SerializationAdapters, utilizing the Java-Serialization, which will be used, once the
	 * main-serialization fails.
	 *
	 * @return a new Instance.
	 */
	static List<SerializationAdapter<Object, String>> getDefaultFallback() {
		final List<SerializationAdapter<Object, String>> toReturn = new ArrayList<>();
		toReturn.add(new PingSerializationAdapter());
		return toReturn;
	}

	/**
	 * Serializes an <code>f</code> into an <code>T</code>.
	 * <p>
	 * Most likely, an Object will be serialized into a String.
	 *
	 * @param f the input type.
	 * @return the serialized input type.
	 * @throws SerializationFailedException if this adapter cannot Serialize the input
	 */
	T get(final F f) throws SerializationFailedException;
}
