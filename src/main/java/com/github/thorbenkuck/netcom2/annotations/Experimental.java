package com.github.thorbenkuck.netcom2.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Shows that this annotated class is not yet tested thoroughly.
 *
 * It might still function as proposed, but it is not recommended by unit-tests.
 *
 * Future: There might be a warning, when an class, annotated with Experimental is compiled
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Experimental {
}
