package com.github.thorbenkuck.netcom2.network.shared.client;

import com.github.thorbenkuck.netcom2.network.shared.DeSerializationAdapter;
import com.github.thorbenkuck.netcom2.network.shared.DecryptionAdapter;
import com.github.thorbenkuck.netcom2.network.shared.EncryptionAdapter;
import com.github.thorbenkuck.netcom2.network.shared.SerializationAdapter;

public class NativeObjectHandler implements ObjectHandler {
	@Override
	public void addFallbackSerialization(SerializationAdapter serializationAdapter) {

	}

	@Override
	public void addFallbackDeserialization(DeSerializationAdapter deSerializationAdapter) {

	}

	@Override
	public void setMainSerialization(SerializationAdapter serializationAdapter) {

	}

	@Override
	public void setMainDeserialization(DeSerializationAdapter deSerializationAdapter) {

	}

	@Override
	public void addEncryptionAdapter(EncryptionAdapter encryptionAdapter) {

	}

	@Override
	public void addDecryptionAdapter(DecryptionAdapter decryptionAdapter) {

	}

	@Override
	public String convert(Object object) {
		return object.getClass().equals(String.class) ? (String) object : null;
	}

	@Override
	public Object convert(String string) {
		return string;
	}
}
