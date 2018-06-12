package com.github.thorbenkuck.netcom2.interfaces;

public interface Module<T extends NetworkInterface> {

	void setup(T t);

}
