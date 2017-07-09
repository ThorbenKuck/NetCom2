package de.thorbenkuck.netcom2.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Shows that this method is not yet tested thoroughly
 */
@Target (ElementType.METHOD)
@Retention (RetentionPolicy.SOURCE)
public @interface Experimantal {
}
