package com.github.thorbenkuck.netcom2.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is to used to signal, that a method-call is executing whatever it wants to do, within another Thread.
 *
 * This should be a sign for anyone, to ensure thread safety
 *
 * It is further recommended to return any kind of synchronization-mechanism (like {@link com.github.thorbenkuck.netcom2.network.shared.Awaiting})
 * but it is certainly not required.
 *
 * Outside of NetCom2s internal Modules, this Annotation is a signal, that you will be seduced to asynchronous behaviour
 * if you call this Method. Therefor you may not be able to work synchronous or in an procedural style.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Asynchronous {
}
