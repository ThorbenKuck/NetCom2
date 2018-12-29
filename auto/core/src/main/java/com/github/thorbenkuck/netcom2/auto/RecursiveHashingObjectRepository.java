package com.github.thorbenkuck.netcom2.auto;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class RecursiveHashingObjectRepository implements ObjectRepository {

	private final Map<Class<?>, Object> mapping = new HashMap<>();

	private <T> T instantiate(Class<T> type) {
		Constructor<?>[] constructors = type.getConstructors();

		for (Constructor<?> tmp : constructors) {
			// This cast is required, because java
			Constructor<T> constructor = (Constructor<T>) tmp;
			Class<?>[] classes = constructor.getParameterTypes();
			List<Object> resolution = new ArrayList<>();
			boolean fail = false;

			for (Class<?> clazz : classes) {
				Object o = get(clazz);
				if (o == null) {
					// This will reduce the loops required
					// just a bit. As soon as we hit a
					// dependency that we cannot resolve,
					// we check the next constructor
					fail = true;
					break;
				} else {
					resolution.add(o);
				}
			}

			if (fail) {
				continue;
			}

			try {
				return constructor.newInstance(resolution.toArray());
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
				throw new IllegalStateException("Could not locate any instance of the type " + type, e);
			}
		}

		throw new IllegalStateException("Could not locate any suitable constructor within the class " + type);
	}

	@Override
	public void add(Object o) {
		mapping.put(o.getClass(), o);
	}

	@Override
	public <T> T get(Class<T> type) {
		T t = (T) mapping.get(type);

		if (t == null) {
			t = instantiate(type);
			mapping.put(type, t);
		}

		return t;
	}
}
