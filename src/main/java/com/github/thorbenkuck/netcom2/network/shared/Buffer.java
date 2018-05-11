package com.github.thorbenkuck.netcom2.network.shared;

import java.util.ArrayList;
import java.util.List;

public class Buffer {

	private byte[] core;
	private int capacity;

	public Buffer(int size) {
		this.capacity = size;
		clear();
	}

	public List<Byte> list() {
		final List<Byte> result = new ArrayList<>();
		for (byte b : core) {
			result.add(b);
		}

		return result;
	}

	public void reallocate(int capacity) {
		this.capacity = capacity;
	}

	public void teardown() {
		this.core = null;
	}

	public void clear() {
		core = new byte[capacity];
	}

	public byte[] array() {
		return core;
	}
}
