package com.github.thorbenkuck.netcom2.interfaces;

public interface Adapter<F, T> {
	T get(F f);
}
