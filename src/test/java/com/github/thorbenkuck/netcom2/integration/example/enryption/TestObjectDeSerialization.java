package com.github.thorbenkuck.netcom2.integration.example.enryption;

import com.github.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;
import com.github.thorbenkuck.netcom2.integration.TestObject;
import com.github.thorbenkuck.netcom2.network.shared.DeSerializationAdapter;

public class TestObjectDeSerialization implements DeSerializationAdapter {
	@Override
	public Object apply(String string) throws DeSerializationFailedException {
		if (!string.startsWith("TestObject")) {
			throw new DeSerializationFailedException("Can only DeSerialize TestObject");
		}
		return new TestObject(string.split("\\|")[1]);
	}
}
