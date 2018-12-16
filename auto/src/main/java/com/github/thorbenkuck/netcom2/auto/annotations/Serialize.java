package com.github.thorbenkuck.netcom2.auto.annotations;

import java.lang.annotation.*;

/**
 * Any annotated method is required to have a parameter (of type {@link Object}) and a return value (of type {@link String}).
 * <p>
 * This method will be wrapped into any {@link com.github.thorbenkuck.netcom2.network.shared.SerializationAdapter}.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@Documented
public @interface Serialize {

	String name() default "";

	boolean autoLoad() default true;

}
