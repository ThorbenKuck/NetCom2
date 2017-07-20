package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import java.io.Serializable;

public class UnRegisterResponse implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private UnRegisterRequest unRegisterRequest;
	private boolean okay;

	public UnRegisterResponse(UnRegisterRequest unRegisterRequest, boolean okay) {
		this.unRegisterRequest = unRegisterRequest;
		this.okay = okay;
	}

	public boolean isOkay() {
		return okay;
	}

	public UnRegisterRequest getRequest() {
		return unRegisterRequest;
	}

	@Override
	public String toString() {
		return "UnRegisterResponse{" +
				"unRegisterRequest=" + unRegisterRequest +
				", okay=" + okay +
				'}';
	}
}
