package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import com.github.thorbenkuck.netcom2.annotations.APILevel;

import java.io.Serializable;

@APILevel
public final class UnRegisterRequest implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private final Class aClass;

	public UnRegisterRequest(final Class aClass) {
		this.aClass = aClass;
	}

	public final Class getCorrespondingClass() {
		return aClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String toString() {
		return "UnRegisterRequest{" +
				"class at interest=" + aClass +
				'}';
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof UnRegisterRequest)) return false;

		UnRegisterRequest that = (UnRegisterRequest) o;

		return aClass.equals(that.aClass);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int hashCode() {
		return aClass.hashCode();
	}
}
