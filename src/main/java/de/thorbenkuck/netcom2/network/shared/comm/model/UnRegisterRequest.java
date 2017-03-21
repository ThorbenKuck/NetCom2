package de.thorbenkuck.netcom2.network.shared.comm.model;

import java.io.Serializable;

public class UnRegisterRequest implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private Class aClass;

	public UnRegisterRequest(Class aClass) {
		this.aClass = aClass;
	}

	public Class getCorrespondingClass() {
		return aClass;
	}
}
