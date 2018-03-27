package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;

import java.util.ArrayList;
import java.util.List;

/**
 * This Adapter will DeSerialize the <code>F</code> into and <code>T</code>.
 *
 * @param <F> the input-type of this adapter
 * @param <T> the output-type of this adapter
 * @version 1.0
 * @since 1.0
 */
public interface DeSerializationAdapter<F, T> {

	/**
	 * Creates the default DeSerializationAdapter, utilizing the Java-Serialization
	 *
	 * @return a new Instance.
	 */
	static DeSerializationAdapter<String, Object> getDefaultJavaSerialization() {
		return new JavaDeSerializationAdapter();
	}

	/**
	 * Creates the default DeSerializationAdapters, utilizing the Java-Serialization, which will be used, once the
	 * main-deserialization fails.
	 *
	 * @return a new Instance.
	 */
	static List<DeSerializationAdapter<String, Object>> getDefaultFallback() {
		List<DeSerializationAdapter<String, Object>> toReturn = new ArrayList<>();
		toReturn.add(new PingDeSerializationAdapter());
		return toReturn;
	}

	/**
	 * DeSerializes an <code>f</code> into an <code>T</code>.
	 * <p>
	 * Most likely, an String will be deSerialized into an Object.
	 *
	 * @param f the input object.
	 * @return the deSerialized input type
	 * @throws DeSerializationFailedException if this adapter cannot Serialize the input
	 */
	T get(final F f) throws DeSerializationFailedException;
}
