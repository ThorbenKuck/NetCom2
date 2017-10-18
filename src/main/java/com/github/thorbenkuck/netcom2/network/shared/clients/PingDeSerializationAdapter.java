package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.Ping;

public class PingDeSerializationAdapter implements DeSerializationAdapter<String, Object> {
	@Asynchronous
	@Override
	public Object get(String s) throws DeSerializationFailedException {
		if (s.startsWith("Ping")) {
			return deSerializePing(s);
		}
		throw new DeSerializationFailedException("[" + getClass().getSimpleName() + "]: " + "Only DeSerialization of Ping supported!");
	}

	private Object deSerializePing(String s) {
		return new Ping(ClientID.fromString(s.split("\\|")[1]));
	}
}
