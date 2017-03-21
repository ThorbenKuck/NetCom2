package de.thorbenkuck.netcom2.interfaces;

public interface Factory<F, T> {

	T create(F f);

}
