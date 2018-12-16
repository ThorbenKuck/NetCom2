package com.github.thorbenkuck.netcom2.auto.annotations;

import java.lang.annotation.*;

/**
 * Handles the connection of a new {@link com.github.thorbenkuck.netcom2.network.shared.clients.Client}.
 * <p>
 * Any annotated method might have one or zero parameters. If one parameter is present, it must be a {@link com.github.thorbenkuck.netcom2.network.shared.clients.Client}
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@Documented
public @interface Connect {

	String className() default "";

}
