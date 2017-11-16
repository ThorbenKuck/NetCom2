package com.github.thorbenkuck.netcom2.interfaces;

@FunctionalInterface
public interface Factory<F, T> {

	T create(final F f);

}
