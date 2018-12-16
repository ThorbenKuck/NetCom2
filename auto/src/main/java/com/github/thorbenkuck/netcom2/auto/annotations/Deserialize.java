package com.github.thorbenkuck.netcom2.auto.annotations;

import java.lang.annotation.*;

/**
 * Any annotated method is required to have a parameter (of type {@link String}) and a return value (of type {@link Object}).
 * <p>
 * This method will be wrapped into any {@link com.github.thorbenkuck.netcom2.network.shared.DeSerializationAdapter}.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@Documented
public @interface Deserialize {

	String name() default "";

	boolean autoLoad() default true;

}
