package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.netcom2.annotations.ReceiveHandler;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

class ReflectionBasedObjectAnalyzer {

	public <T> Optional<Method> getResponsibleMethod(Object o, Class<T> clazz) {
		return getCorrespondingMethod(o, clazz);
	}

	private <T> Optional<Method> getCorrespondingMethod(Object o, Class<T> clazz) {
		for (Method method : o.getClass().getDeclaredMethods()) {
//			if(!Modifier.isPrivate(method.getModifiers())) {
//
//			}
			if (isAnnotationPresent(ReceiveHandler.class, method)) {
				ReceiveHandler receiveHandler = method.getAnnotation(ReceiveHandler.class);
				if (receiveHandler.active() && containsOnlyAskedParameter(method, clazz)) {
					return Optional.of(method);
				}
			}
		}

		return Optional.empty();
	}

	private boolean isAnnotationPresent(Class<? extends Annotation> annotation, Method method) {
		return method.getAnnotation(annotation) != null;
	}

	private <T> boolean containsOnlyAskedParameter(Method method, Class<T> clazz) {
		boolean contains = false;
		for (Class clazzToCheck : method.getParameterTypes()) {
			if (! clazzToCheck.equals(Connection.class) && ! clazzToCheck.equals(Session.class)) {
				if (clazzToCheck.equals(clazz)) {
					contains = true;
				} else {
					// If it contains anything else than a Session, a Connection of the object to Receive, return immediately!
					return false;
				}
			}
		}
		return contains;
	}

	private <T> Optional<Class<T>> getResponsibleClassFromMethod(Method method) {
		if (method.getParameterCount() == 0) {
			return Optional.empty();
		}
		return Optional.empty();
	}

}
