package com.github.thorbenkuck.netcom2.integration;

import com.github.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;
import com.github.thorbenkuck.netcom2.network.shared.clients.DeSerializationAdapter;

public class TestDeSerializer implements DeSerializationAdapter<String, Object> {
	@Override
	public Object get(String s) throws DeSerializationFailedException {
		if (s.startsWith("2")) {
			return new TestObjectTwo(s.split("\\|")[1]);
		}

		throw new DeSerializationFailedException("Can only Serialize TestObjectTwo!");
	}
}
