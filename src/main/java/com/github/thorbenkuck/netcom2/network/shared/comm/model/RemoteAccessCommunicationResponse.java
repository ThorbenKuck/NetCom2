package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import com.github.thorbenkuck.netcom2.annotations.APILevel;

import java.io.Serializable;
import java.util.UUID;

@APILevel
public final class RemoteAccessCommunicationResponse implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private final UUID uuid;
	private final Throwable throwable;
	private final Object result;

	public RemoteAccessCommunicationResponse(final UUID uuid, final Throwable throwable, final Object result) {
		this.uuid = uuid;
		this.throwable = throwable;
		this.result = result;
	}

	public final UUID getUuid() {
		return uuid;
	}

	public final Throwable getThrownThrowable() {
		return throwable;
	}

	public final Object getResult() {
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String toString() {
		return "RemoteAccessCommunicationResponse{" +
				"uuid=" + uuid +
				", throwable=" + throwable +
				", result=" + result +
				'}';
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof RemoteAccessCommunicationResponse)) return false;

		RemoteAccessCommunicationResponse that = (RemoteAccessCommunicationResponse) o;

		if (!uuid.equals(that.uuid)) return false;
		if (!throwable.equals(that.throwable)) return false;
		return result.equals(that.result);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int hashCode() {
		int result1 = uuid.hashCode();
		result1 = 31 * result1 + throwable.hashCode();
		result1 = 31 * result1 + result.hashCode();
		return result1;
	}
}
