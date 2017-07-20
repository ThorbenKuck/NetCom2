package com.github.thorbenkuck.netcom2.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This shows, that an Annotated Method is exposed to be used by the implementing developer. Therefor it should be accessible
 */
@Target (ElementType.METHOD)
@Retention (RetentionPolicy.SOURCE)
public @interface Exposed {
}
