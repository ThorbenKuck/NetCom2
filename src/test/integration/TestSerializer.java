package com.github.thorbenkuck.netcom2.integration;

import com.github.thorbenkuck.netcom2.exceptions.SerializationFailedException;
import com.github.thorbenkuck.netcom2.network.shared.clients.SerializationAdapter;

public class TestSerializer implements SerializationAdapter<Object, String> {
	@Override
	public String get(Object o) throws SerializationFailedException {
		if (o.getClass().equals(TestObjectTwo.class)) {
			return "2|" + ((TestObjectTwo) o).getCurrentElement();
		}

		throw new SerializationFailedException("Can only Serialize TestObjectTwo!");
	}
}
