package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.Ping;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

public class PingDeSerializationAdapter implements DeSerializationAdapter<String, Object> {
	private Object deSerializePing(final String s) {
		return new Ping(ClientID.fromString(s.split("\\|")[1]));
	}

	@Asynchronous
	@Override
	public Object get(final String s) throws DeSerializationFailedException {
		NetCom2Utils.parameterNotNull(s);
		if (s.startsWith("Ping")) {
			return deSerializePing(s);
		}
		throw new DeSerializationFailedException(
				"[" + getClass().getSimpleName() + "]: " + "Only DeSerialization of Ping supported!");
	}
}
