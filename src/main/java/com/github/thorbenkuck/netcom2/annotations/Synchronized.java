package com.github.thorbenkuck.netcom2.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation states that an Class is Synchronized and does not do anything inside of other Threads at any point in
 * time if any Method is called.
 *
 * So it is not possible to have an class be annotated with @Synchronized and any method with {@link Asynchronous}.
 *
 * Future: Create an Annotation-Processor, that ensures that no Method is annotated with {@link Asynchronous}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Synchronized {
}
