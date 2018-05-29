package com.github.thorbenkuck.netcom2.system.example.enryption;

import com.github.thorbenkuck.netcom2.exceptions.SerializationFailedException;
import com.github.thorbenkuck.netcom2.system.TestObject;;
import com.github.thorbenkuck.netcom2.network.shared.clients.SerializationAdapter;

public class TestObjectSerialization implements SerializationAdapter<Object, String> {
	@Override
	public String get(final Object o) throws SerializationFailedException {
		if(!TestObject.class.equals(o)) {
			throw new SerializationFailedException("Can only Serialize com.github.thorbenkuck.netcom2.system.TestObject;");
		}
		return "TestObject|"+((TestObject)o).getHello();
	}
}
