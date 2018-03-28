package com.github.thorbenkuck.netcom2.integration.example.enryption;

import com.github.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;
import com.github.thorbenkuck.netcom2.integration.TestObject;
import com.github.thorbenkuck.netcom2.network.shared.clients.DeSerializationAdapter;

public class TestObjectDeSerialization implements DeSerializationAdapter<String, Object> {
	@Override
	public Object get(final String s) throws DeSerializationFailedException {
		if(!s.startsWith("TestObject")) {
			throw new DeSerializationFailedException("Can only DeSerialize TestObject");
		}
		return new TestObject(s.split("\\|")[1]);
	}
}
