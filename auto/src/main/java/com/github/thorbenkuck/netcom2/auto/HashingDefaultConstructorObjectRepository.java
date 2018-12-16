package com.github.thorbenkuck.netcom2.auto;

import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

final class HashingDefaultConstructorObjectRepository implements ObjectRepository {

	private final Map<Class<?>, Object> mapping = new HashMap<>();

	private <T> T instantiate(Class<T> type) {
		Constructor<T> constructor;
		try {
			constructor = type.getConstructor();
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException("Could not locate any instance of the type " + type, e);
		}

		T t = null;
		try {
			t = constructor.newInstance();
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new IllegalStateException("Could not locate any instance of the type " + type, e);
		}

		if (t == null) {
			throw new IllegalStateException("Could not locate any instance of the type " + type);
		}

		return t;
	}

	@Override
	public void add(Object o) {
		NetCom2Utils.parameterNotNull(o);
		mapping.put(o.getClass(), o);
	}

	@Override
	public <T> T get(Class<T> type) {
		T t = (T) mapping.get(type);

		if (t == null) {
			t = instantiate(type);
		}

		return t;
	}
}
