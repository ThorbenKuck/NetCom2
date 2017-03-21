package de.thorbenkuck.netcom2.network.shared.comm.model;

import java.io.Serializable;

public class CachePush implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private Object object;

	public CachePush(Object object) {
		this.object = object;
	}

	public Object getObject() {
		return object;
	}
}
