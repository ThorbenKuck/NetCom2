package de.thorbenkuck.netcom2.interfaces;

public interface Adapter<F, T> {
	T get(F f);
}
