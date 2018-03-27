package com.github.thorbenkuck.netcom2.annotations;

import java.lang.annotation.*;

/**
 * This annotation is to used to signal, that the annotated method is executing some procedure within another Thread.
 * <p>
 * This should be a sign for anyone, to ensure thread safety
 * <p>
 * It is further recommended to return any kind of synchronization-mechanism (like {@link com.github.thorbenkuck.netcom2.network.shared.Awaiting})
 * but it is certainly not required.
 * <p>
 * Outside of NetCom2s internal Modules, this Annotation is a signal, that you will be seduced to asynchronous behaviour
 * if you call this Method. Therefor you may not be able to work synchronous or in an procedural style.
 *
 * This Annotation is only used, if the method is <b>directly</b> extracting something into another thread. If (for example)
 * <code>Connection#write</code> is called, this is not reason to apply this annotation. That is because, whether or not
 * the Connection extracts something into another Thread, depends on the implementation.
 *
 * @version 1.0
 * @since 1.0
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Asynchronous {
}
