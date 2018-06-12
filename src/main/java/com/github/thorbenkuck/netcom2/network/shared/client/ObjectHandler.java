package com.github.thorbenkuck.netcom2.network.shared.client;

import com.github.thorbenkuck.netcom2.network.shared.DeSerializationAdapter;
import com.github.thorbenkuck.netcom2.network.shared.DecryptionAdapter;
import com.github.thorbenkuck.netcom2.network.shared.EncryptionAdapter;
import com.github.thorbenkuck.netcom2.network.shared.SerializationAdapter;

public interface ObjectHandler {

	static ObjectHandler create() {
		return new NativeObjectHandler();
	}

	void addFallbackSerialization(SerializationAdapter serializationAdapter);

	void addFallbackDeserialization(DeSerializationAdapter deSerializationAdapter);

	void setMainSerialization(SerializationAdapter serializationAdapter);

	void setMainDeserialization(DeSerializationAdapter deSerializationAdapter);

	void addEncryptionAdapter(EncryptionAdapter encryptionAdapter);

	void addDecryptionAdapter(DecryptionAdapter decryptionAdapter);

	String convert(Object object);

	Object convert(String string);

}
