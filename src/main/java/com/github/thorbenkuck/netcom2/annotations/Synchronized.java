package com.github.thorbenkuck.netcom2.annotations;

import java.lang.annotation.*;

/**
 * This annotation states that an Class is Synchronized and does not do anything inside of other Threads at any point in
 * time if any Method is called.
 * <p>
 * So it is not possible to have an class be annotated with @Synchronized and any method with {@link Asynchronous}.
 * <p>
 * Note: If an class is annotated with this annotation and calls another class, which in fact does extract procedures into
 * another Thread, this is not an error. Since it is not clear, that the implementation will always extract into another
 * Thread. Everything that is important, is that the annotated class does not <b>directly</b> extract an procedure into
 * another Thread!
 * <p>
 * So, if you are using a class that, will always extract something into another Thread, like {@link Thread} (duh) or the
 * {@link java.util.concurrent.ExecutorService}, this annotation is not allowed!
 * <p>
 * Future: Create an Annotation-Processor, that ensures that no Method is annotated with {@link Asynchronous}.
 *
 * @version 1.0
 * @since 1.0
 */
@Deprecated
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Synchronized {
}
