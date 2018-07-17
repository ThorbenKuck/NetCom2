package com.github.thorbenkuck.netcom2.file;

import java.io.Serializable;

public class FileResponse implements Serializable {

	private final byte[] data;

	public FileResponse(byte[] data) {
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}
}
