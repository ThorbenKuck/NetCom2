package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.exceptions.SerializationFailedException;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.Ping;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

public class PingSerializationAdapter implements SerializationAdapter<Object, String> {
	private String serializePing(final Ping o) {
		return "Ping|" + o.getId();
	}

	@Asynchronous
	@Override
	public String get(final Object o) throws SerializationFailedException {
		NetCom2Utils.parameterNotNull(o);
		if (o.getClass().equals(Ping.class)) {
			return serializePing((Ping) o);
		}
		throw new SerializationFailedException(
				"[" + getClass().getSimpleName() + "]: " + "Only Ping-Serialization supported!");
	}
}
