package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import com.github.thorbenkuck.netcom2.annotations.APILevel;

import java.io.Serializable;
import java.util.Arrays;
import java.util.UUID;

@APILevel
public class RemoteAccessCommunicationRequest implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private final String methodName;
	private final Class<?> clazz;
	private final UUID uuid;
	private final Object[] parameters;

	public RemoteAccessCommunicationRequest(final String methodName, final Class<?> clazz, final UUID uuid, final Object[] parameters) {
		this.methodName = methodName;
		this.clazz = clazz;
		this.uuid = uuid;
		this.parameters = parameters;
	}

	public String getMethodName() {
		return methodName;
	}

	public UUID getUuid() {
		return uuid;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public Object[] getParameters() {
		return parameters;
	}

	@Override
	public String toString() {
		return "RemoteAccessCommunicationRequest{" +
				"methodName='" + methodName + '\'' +
				", clazz=" + clazz +
				", uuid=" + uuid +
				", parameters=" + Arrays.toString(parameters) +
				'}';
	}
}
