package com.github.thorbenkuck.netcom2.auto.annotations;

import java.lang.annotation.*;

/**
 * Annotated methods handle the receiving of specific objects.
 * <p>
 * As the {@link com.github.thorbenkuck.netcom2.network.shared.ReceiveFamily}, a method must at least contain an object.
 * This object must not be a {@link com.github.thorbenkuck.netcom2.network.shared.Session}, nor a {@link com.github.thorbenkuck.netcom2.network.shared.connections.ConnectionContext}.
 * Both might be added to the same method additionally to the first object.
 * <p>
 * If no type can be detected (i.e. no specific object is defined), compilation will fail. If multiple types are detected
 * (i.e. multiple specific objects are defined), the compilation will fail as well.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@Documented
public @interface Register {

	String name() default "";

	boolean autoLoad() default true;

}
