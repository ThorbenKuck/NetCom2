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
 * This Annotation has to be present at the requested class. So if the Client requests an RemoteObject via an interface,
 * the interface has to declare this annotation at its Method
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface IgnoreRemoteExceptions {

	Class<? extends Exception>[] exceptTypes() default {};

}
