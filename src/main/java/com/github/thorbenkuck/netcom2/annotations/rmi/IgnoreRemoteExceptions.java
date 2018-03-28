package com.github.thorbenkuck.netcom2.annotations.rmi;

import java.lang.annotation.*;

/**
 * Tells the {@link com.github.thorbenkuck.netcom2.network.client.JavaInvocationHandlerProducer} to ignore all Exceptions
 * that it received from the Remote-Source.
 * <p>
 * Any Exception that should be thrown anyway can be marked by using the {@code exceptTypes} method.
 * <p>
 * This Annotation exists mostly to ensure security, so that the Client does not receive an Exception thrown by the server
 * and therefore is able to see the Stacktrace of the Server.
 * <p>
 * This Annotation has to be present at the requested class/interface, which should be proxied. So if the Client requests
 * a RemoteObject via an interface, the interface has to declare this annotation at its Method.
 * <p>
 * If this Annotation is placed at the Class description AND the method, which ultimately will throw an Exception, the
 * annotation placed at the method will be used over the class-annotation. This means, method annotations override any
 * other annotation.
 *
 * @version 1.0
 * @since 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface IgnoreRemoteExceptions {

	/**
	 * With this method, you can declare which Exceptions should be thrown.
	 * <p>
	 * By default, this Method returns an empty class array. You can add any class that is subclass of Exception to it, to signal
	 * that this Exception should be thrown anyway.
	 * <p>
	 * Note: The thrown Exception will be encapsulated in an {@link com.github.thorbenkuck.netcom2.exceptions.RemoteRequestException}
	 * if it isn't already.
	 * <p>
	 * You may filter any Exception with that, but you cannot filter out the {@link com.github.thorbenkuck.netcom2.exceptions.RemoteObjectNotRegisteredException}.
	 * If you want to prohibit this, you should use {@link com.github.thorbenkuck.netcom2.network.client.RemoteObjectFactory#setFallback(Class, Runnable)}
	 *
	 * @return an array of Exception Types, that should be thrown regardless of the provided Annotation
	 */
	Class<? extends Exception>[] exceptTypes() default {};

}
