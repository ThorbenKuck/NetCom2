package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import com.github.thorbenkuck.netcom2.annotations.APILevel;

import java.io.Serializable;
import java.util.UUID;

@APILevel
public class RemoteAccessCommunicationResponse implements Serializable {

	private final UUID uuid;
	private final Throwable throwable;
	private final Object result;
	private static final long serialVersionUID = 4414647424220391756L;

	public RemoteAccessCommunicationResponse(final UUID uuid, final Throwable throwable, final Object result) {
		this.uuid = uuid;
		this.throwable = throwable;
		this.result = result;
	}

	public UUID getUuid() {
		return uuid;
	}

	public Throwable getThrownThrowable() {
		return throwable;
	}

	public Object getResult() {
		return result;
	}

	@Override
	public String toString() {
		return "RemoteAccessCommunicationResponse{" +
				"uuid=" + uuid +
				", throwable=" + throwable +
				", result=" + result +
				'}';
	}
}
