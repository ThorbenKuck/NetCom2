package com.github.thorbenkuck.netcom2.annotations.remoteObjects;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tells the {@link com.github.thorbenkuck.netcom2.network.client.JavaInvocationHandlerProducer} to ignore all Exceptions
 * that he received from the Remote-Source.
 *
 * Any Exception that should be thrown anyways can be marked by using the {@code exceptTypes} method.
 *
 * This Annotation exists mostly to ensure security, so that the Client does not receive an Exception, thrown by the server
 * and therefor is able to see the Stacktrace of the Server.
 *
 * This Annotation has to be present at the requested class. So if the Client requests an RemoteObject via an interface,
 * the interface has to declare this annotation at its Method
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface IgnoreRemoteExceptions {

	/**
	 * With this method, you can declare, which Exceptions should be thrown.
	 *
	 * By Default, this Method returns an empty class array. You can add any class that is subclass of Exception to it, to signal
	 * that this Exception should be thrown anyways.
	 *
	 * Note: The thrown Exception will be encapsulated in an {@link com.github.thorbenkuck.netcom2.exceptions.RemoteRequestException}
	 * if it is'nt already.
	 *
	 * @return an array of Exception Types, that should be thrown regardless of the provided Annotation
	 */
	Class<? extends Exception>[] exceptTypes() default {};

}
