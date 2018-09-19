package com.github.thorbenkuck.netcom2.integration.example.enryption;

import com.github.thorbenkuck.netcom2.exceptions.SerializationFailedException;
import com.github.thorbenkuck.netcom2.integration.TestObject;
import com.github.thorbenkuck.netcom2.network.shared.SerializationAdapter;

public class TestObjectSerialization implements SerializationAdapter {
	@Override
	public String apply(final Object o) throws SerializationFailedException {
		if (!TestObject.class.equals(o)) {
			throw new SerializationFailedException("Can only Serialize com.github.thorbenkuck.netcom2.integration.TestObject");
		}
		return "TestObject|" + ((TestObject) o).getContent();
	}
}
