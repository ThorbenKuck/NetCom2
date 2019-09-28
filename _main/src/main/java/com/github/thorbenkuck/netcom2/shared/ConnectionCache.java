package com.github.thorbenkuck.netcom2.shared;

public interface ConnectionCache {

	void append(final byte[] bytes);

	byte[] take();
}
