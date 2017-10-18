package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.exceptions.SerializationFailedException;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.Ping;

public class PingSerializationAdapter implements SerializationAdapter<Object, String> {
	@Asynchronous
	@Override
	public String get(Object o) throws SerializationFailedException {
		if (o.getClass().equals(Ping.class)) {
			return serializePing((Ping) o);
		}
		throw new SerializationFailedException("[" + getClass().getSimpleName() + "]: " + "Only Ping-Serialization supported!");
	}

	private String serializePing(Ping o) {
		return "Ping|" + o.getId();
	}
}
