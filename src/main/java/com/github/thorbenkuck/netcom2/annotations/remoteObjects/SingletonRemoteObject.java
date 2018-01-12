package com.github.thorbenkuck.netcom2.annotations.remoteObjects;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This will signal the RemoteObjectFactory, that the InvocationHandler
 * for this Type should be reused instead of newly created every request.
 *
 * Since the instantiation of the InvocationHandler is by default lazy, there is no need to declare a lazy instantiation
 *
 * Any Class annotated with this Annotation will therefor be instantiated only once, but kept stored!
 * This means, that even if you clear an local instance, the created Class will be present in the RemoteObjectFactory!
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SingletonRemoteObject {
}
