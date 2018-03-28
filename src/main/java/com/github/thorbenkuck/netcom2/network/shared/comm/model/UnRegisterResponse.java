package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import com.github.thorbenkuck.netcom2.annotations.APILevel;

import java.io.Serializable;

@APILevel
public final class UnRegisterResponse implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private final UnRegisterRequest unRegisterRequest;
	private final boolean okay;

	public UnRegisterResponse(final UnRegisterRequest unRegisterRequest, boolean okay) {
		this.unRegisterRequest = unRegisterRequest;
		this.okay = okay;
	}

	public final boolean isOkay() {
		return okay;
	}

	public final UnRegisterRequest getRequest() {
		return unRegisterRequest;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String toString() {
		return "UnRegisterResponse{" +
				"unRegisterRequest=" + unRegisterRequest +
				", okay=" + okay +
				'}';
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof UnRegisterResponse)) return false;

		UnRegisterResponse that = (UnRegisterResponse) o;

		if (okay != that.okay) return false;
		return unRegisterRequest.equals(that.unRegisterRequest);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int hashCode() {
		int result = unRegisterRequest.hashCode();
		result = 31 * result + (okay ? 1 : 0);
		return result;
	}
}
