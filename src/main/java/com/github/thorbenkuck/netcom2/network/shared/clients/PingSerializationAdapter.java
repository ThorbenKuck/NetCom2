package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.annotations.Tested;
import com.github.thorbenkuck.netcom2.exceptions.SerializationFailedException;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.Ping;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

/**
 * This DeSerializationAdapter will deserialize an Ping, serialized with the {@link PingSerializationAdapter}
 *
 * @version 1.0
 * @since 1.0
 */
@Synchronized
@Tested(responsibleTest = "com.github.thorbenkuck.netcom2.network.shared.clients.PingSerializationAdapterTest")
public class PingSerializationAdapter implements SerializationAdapter<Object, String> {
	private String serializePing(final Ping o) {
		return "Ping|" + o.getId();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the provided Object is null
	 */
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
