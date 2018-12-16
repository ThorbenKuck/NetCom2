package com.github.thorbenkuck.netcom2.auto;

public interface ObjectRepository {

	static ObjectRepository hashingDefault() {
		return new HashingDefaultConstructorObjectRepository();
	}

	static ObjectRepository hashingRecursive() {
		return new RecursiveHashingObjectRepository();
	}

	void add(Object o);

	<T> T get(Class<T> type);

}
