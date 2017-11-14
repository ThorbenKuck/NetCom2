package com.github.thorbenkuck.netcom2.interfaces;

public interface Mutex {

	void acquire() throws InterruptedException;

	void release();

}
