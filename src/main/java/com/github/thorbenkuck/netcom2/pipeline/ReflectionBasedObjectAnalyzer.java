package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.ReceiveHandler;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * This class adds the capability of finding a method that takes a specific class as parameter.
 * <p>
 * This class is meant for NetCom2 internal use only.
 *
 * @since 1.0
 * @version 1.0
 */
@APILevel
@Synchronized
class ReflectionBasedObjectAnalyzer {

	/**
	 * Gets the method for the receive handler with the given class on the specified object.
	 *
	 * @param o The object to analyse
	 * @param clazz The parameter type to look for
	 * @param <T> The type of the parameter
	 * @return An optional of the method, may be empty
	 */
	private <T> Optional<Method> getCorrespondingMethod(final Object o, final Class<T> clazz) {
		for (Method method : o.getClass().getDeclaredMethods()) {
			if (isAnnotationPresent(ReceiveHandler.class, method)) {
				final ReceiveHandler receiveHandler = method.getAnnotation(ReceiveHandler.class);
				if (receiveHandler.active() && containsOnlyAskedParameter(method, clazz)) {
					return Optional.of(method);
				}
			}
		}

		return Optional.empty();
	}

	/**
	 * Whether the method is annotated with the specified annotation or not.
	 *
	 * @param annotation The annotation to check for
	 * @param method The method to analyse
	 * @return True, if the annotation is present, false otherwise
	 */
	private boolean isAnnotationPresent(final Class<? extends Annotation> annotation, final Method method) {
		return method.getAnnotation(annotation) != null;
	}

	/**
	 * Find out if the specified method contains only the parameter specified, and it is not a Connection or Session.
	 *
	 * @param method The method to analyse
	 * @param clazz The class to look for
	 * @param <T> The type to look for
	 * @return True if yes, false if no
	 */
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

	/**
	 * Get the method on the specified object that is responsible for the specified class.
	 * <p>
	 * This method is meant for internal use only.
	 *
	 * @param o The object to analyse
	 * @param clazz The class to look for
	 * @param <T> The type to look for
	 * @return An optional of the responsible method, may be empty
	 */
	@APILevel
	<T> Optional<Method> getResponsibleMethod(final Object o, final Class<T> clazz) {
		NetCom2Utils.parameterNotNull(o, clazz);
		return getCorrespondingMethod(o, clazz);
	}

}
