package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import java.io.Serializable;

public class RegisterResponse implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private RegisterRequest request;
	private boolean okay;

	public RegisterResponse(RegisterRequest request, boolean okay) {
		this.request = request;
		this.okay = okay;
	}

	public boolean isOkay() {
		return okay;
	}

	public RegisterRequest getRequest() {
		return request;
	}

	@Override
	public String toString() {
		return "RegisterResponse{" +
				"request=" + request +
				", okay=" + okay +
				'}';
	}
}
