package com.github.thorbenkuck.netcom2.interfaces;

@FunctionalInterface
public interface TriPredicate<T, U, V> {

	boolean test(T t, U u, V v);

}
