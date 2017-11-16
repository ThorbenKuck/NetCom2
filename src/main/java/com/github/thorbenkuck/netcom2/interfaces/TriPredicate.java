package com.github.thorbenkuck.netcom2.interfaces;

@FunctionalInterface
public interface TriPredicate<T, U, V> {

	boolean test(final T t, final U u, final V v);

}
