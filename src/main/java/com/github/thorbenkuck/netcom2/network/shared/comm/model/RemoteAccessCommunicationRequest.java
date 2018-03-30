package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import com.github.thorbenkuck.netcom2.annotations.APILevel;

import java.io.Serializable;
import java.util.Arrays;
import java.util.UUID;

@APILevel
public final class RemoteAccessCommunicationRequest implements Serializable {

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

	public final String getMethodName() {
		return methodName;
	}

	public final UUID getUuid() {
		return uuid;
	}

	public final Class<?> getClazz() {
		return clazz;
	}

	public final Object[] getParameters() {
		return parameters;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String toString() {
		return "RemoteAccessCommunicationRequest{" +
				"methodName='" + methodName + '\'' +
				", clazz=" + clazz +
				", uuid=" + uuid +
				", parameters=" + Arrays.toString(parameters) +
				'}';
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof RemoteAccessCommunicationRequest)) return false;

		RemoteAccessCommunicationRequest that = (RemoteAccessCommunicationRequest) o;

		if (!methodName.equals(that.methodName)) return false;
		if (!clazz.equals(that.clazz)) return false;
		if (!uuid.equals(that.uuid)) return false;
		// Probably incorrect - comparing Object[] arrays with Arrays.equals
		return Arrays.equals(parameters, that.parameters);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int hashCode() {
		int result = methodName.hashCode();
		result = 31 * result + clazz.hashCode();
		result = 31 * result + uuid.hashCode();
		result = 31 * result + Arrays.hashCode(parameters);
		return result;
	}
}
