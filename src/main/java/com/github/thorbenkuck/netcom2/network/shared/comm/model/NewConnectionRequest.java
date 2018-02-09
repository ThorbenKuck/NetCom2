package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import com.github.thorbenkuck.netcom2.annotations.APILevel;

import java.io.Serializable;

@APILevel
public class NewConnectionRequest implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private final Class key;

	public NewConnectionRequest(Class key) {
		this.key = key;
	}

	public Class getKey() {
		return key;
	}

	@Override
	public String toString() {
		return "NewConnectionRequest{" +
				"key=" + key +
				'}';
	}
}
