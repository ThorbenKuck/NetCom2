package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import java.io.Serializable;
import java.util.UUID;

public class RemoteAccessCommunicationModelRequest implements Serializable {

	private final String methodName;
	private final Class<?> clazz;
	private final UUID uuid;
	private final Object[] parameters;
	private static final long serialVersionUID = 4414647424220391756L;

	public RemoteAccessCommunicationModelRequest(final String methodName, final Class<?> clazz, final UUID uuid, final Object[] parameters) {
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
}
