package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import java.io.Serializable;
import java.util.UUID;

public class RemoteAccessCommunicationModelResponse implements Serializable {

	private final UUID uuid;
	private final Throwable throwable;
	private final Object result;
	private static final long serialVersionUID = 4414647424220391756L;

	public RemoteAccessCommunicationModelResponse(final UUID uuid, final Throwable throwable, final Object result) {
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
}
