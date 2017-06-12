package de.thorbenkuck.netcom2.interfaces;

@FunctionalInterface
public interface TriConsumer<T, U, V> {

	void accept(T t, U u, V v);
}
