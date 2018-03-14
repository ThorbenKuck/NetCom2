package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import com.github.thorbenkuck.netcom2.annotations.APILevel;

import java.io.Serializable;

@APILevel
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
