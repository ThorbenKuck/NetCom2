package de.thorbenkuck.netcom2.network.shared;

@FunctionalInterface
public interface Feasible<T> {

	void tryAccept(T t);

	default boolean remove() {
		return true;
	}

	default boolean acceptable(Object object) {
		return object != null;
	}

}
