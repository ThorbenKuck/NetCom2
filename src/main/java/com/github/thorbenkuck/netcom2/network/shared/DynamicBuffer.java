package com.github.thorbenkuck.netcom2.network.shared;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DynamicBuffer {

	private final Lock access = new ReentrantLock(true);
	private byte[] core = new byte[0];

	public void teardown() {
		this.core = null;
	}

	public void clear() {
		core = new byte[0];
	}

	public byte[] array() {
		return core;
	}

	public void append(byte b) {
		try {
			access.lock();
			byte[] temp = new byte[core.length + 1];
			for (int i = 0; i < temp.length - 1; i++) {
				temp[i] = core[i];
			}
			temp[temp.length - 1] = b;
			core = temp;
		} finally {
			access.unlock();
		}
	}

	public void append(byte[] bytes) {
		try {
			access.lock();
			byte[] temp = new byte[core.length + bytes.length];
			int offset = 0;
			for (int i = 0; i < core.length; i++) {
				temp[i] = core[i];
				++offset;
			}
			for (int i = 0; i < bytes.length; i++) {
				temp[i + offset] = bytes[i];
			}
			core = temp;
		} finally {
			access.unlock();
		}
	}

	public int size() {
		return core.length;
	}
}
