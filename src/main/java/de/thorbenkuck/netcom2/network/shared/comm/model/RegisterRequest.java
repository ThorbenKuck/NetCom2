package de.thorbenkuck.netcom2.network.shared.comm.model;

import java.io.Serializable;

public class RegisterRequest implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;

	private Class aClass;

	public RegisterRequest(Class aClass) {
		this.aClass = aClass;
	}

	public Class<?> getCorrespondingClass() {
		return aClass;
	}

	@Override
	public String toString() {
		return "RegisterRequest{" +
				"class of interest=" + aClass +
				'}';
	}
}
