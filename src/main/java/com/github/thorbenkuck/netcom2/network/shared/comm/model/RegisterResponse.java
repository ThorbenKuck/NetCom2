package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import com.github.thorbenkuck.netcom2.annotations.APILevel;

import java.io.Serializable;

@APILevel
public final class RegisterResponse implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private final RegisterRequest request;
	private final boolean okay;

	public RegisterResponse(final RegisterRequest request, boolean okay) {
		this.request = request;
		this.okay = okay;
	}

	public final boolean isOkay() {
		return okay;
	}

	public final RegisterRequest getRequest() {
		return request;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String toString() {
		return "RegisterResponse{" +
				"request=" + request +
				", okay=" + okay +
				'}';
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof RegisterResponse)) return false;

		RegisterResponse that = (RegisterResponse) o;

		if (okay != that.okay) return false;
		return request.equals(that.request);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int hashCode() {
		int result = request.hashCode();
		result = 31 * result + (okay ? 1 : 0);
		return result;
	}
}
