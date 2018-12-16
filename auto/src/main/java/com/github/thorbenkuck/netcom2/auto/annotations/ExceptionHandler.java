package com.github.thorbenkuck.netcom2.auto.annotations;

import java.lang.annotation.*;

/**
 * Handle unhandled Exceptions, throw internally within every component
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@Documented
public @interface ExceptionHandler {
}
