package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.ReceiveHandler;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

@APILevel
class ReflectionBasedObjectAnalyzer {

	private <T> Optional<Method> getCorrespondingMethod(final Object o, final Class<T> clazz) {
		for (Method method : o.getClass().getDeclaredMethods()) {
//			if(!Modifier.isPrivate(method.getModifiers())) {
//
//			}
			if (isAnnotationPresent(ReceiveHandler.class, method)) {
				final ReceiveHandler receiveHandler = method.getAnnotation(ReceiveHandler.class);
				if (receiveHandler.active() && containsOnlyAskedParameter(method, clazz)) {
					return Optional.of(method);
				}
			}
		}

		return Optional.empty();
	}

	private boolean isAnnotationPresent(final Class<? extends Annotation> annotation, final Method method) {
		return method.getAnnotation(annotation) != null;
	}

	private <T> boolean containsOnlyAskedParameter(final Method method, final Class<T> clazz) {
		boolean contains = false;
		for (final Class clazzToCheck : method.getParameterTypes()) {
			if (!clazzToCheck.equals(Connection.class) && !clazzToCheck.equals(Session.class)) {
				if (clazzToCheck.equals(clazz)) {
					contains = true;
				} else {
					// If it contains anything else
					// than a Session, a Connection
					// or the object to Receive, return immediately!
					// We do not want this, because
					// we only know those 3 types here.
					return false;
				}
			}
		}
		return contains;
	}

	@APILevel
	<T> Optional<Method> getResponsibleMethod(final Object o, final Class<T> clazz) {
		NetCom2Utils.parameterNotNull(o, clazz);
		return getCorrespondingMethod(o, clazz);
	}

}
