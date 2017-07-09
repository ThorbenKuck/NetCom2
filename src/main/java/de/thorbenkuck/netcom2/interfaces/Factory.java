package de.thorbenkuck.netcom2.interfaces;

@FunctionalInterface
public interface Factory<F, T> {

	T create(F f);

}
