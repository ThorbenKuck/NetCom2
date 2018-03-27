package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.annotations.Tested;
import com.github.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.Ping;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

/**
 * This DeSerializationAdapter will deserialize an Ping, serialized with the {@link PingSerializationAdapter}
 *
 * @version 1.0
 * @since 1.0
 */
@Synchronized
@Tested(responsibleTest = "com.github.thorbenkuck.netcom2.network.shared.clients.PingDeSerializationAdapterTest")
public class PingDeSerializationAdapter implements DeSerializationAdapter<String, Object> {
	private Object deSerializePing(final String s) {
		return new Ping(ClientID.fromString(s.split("\\|")[1]));
	}

	/**
	 * {@inheritDoc}
	 */
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
