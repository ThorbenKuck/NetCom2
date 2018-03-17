package com.github.thorbenkuck.netcom2.annotations.rmi;

import java.lang.annotation.*;

/**
 * This will signal the RemoteObjectFactory, that the InvocationHandler
 * for this Type should be reused instead of newly created every request.
 * <p>
 * Since the instantiation of the InvocationHandler is by default lazy, there is no need to declare a lazy instantiation
 * <p>
 * Any Class annotated with this Annotation will therefor be instantiated only once, but kept stored!
 * This means, that even if you clear an local instance, the created Class will be present in the RemoteObjectFactory!
 * <p>
 * By Design, every instantiation is lazy. You do not need to declare it and you cannot change this behaviour.
 */
@Documented
@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.TYPE)
public @interface SingletonRemoteObject {
}
