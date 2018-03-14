package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import com.github.thorbenkuck.netcom2.annotations.APILevel;

import java.io.Serializable;

@APILevel
public class UnRegisterRequest implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private Class aClass;

	public UnRegisterRequest(Class aClass) {
		this.aClass = aClass;
	}

	public Class getCorrespondingClass() {
		return aClass;
	}

	@Override
	public String toString() {
		return "UnRegisterRequest{" +
				"class at interest=" + aClass +
				'}';
	}
}
