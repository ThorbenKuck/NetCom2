package de.thorbenkuck.netcom2.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is to be implemented properly! It should look something like this:
 *
 * <code>
 *     TestReceiver testReceiver = ...
 *     *Start.getCommunicationRegistration.register(TestObject.class).to(testReceiver);
 * </code>
 *
 * and
 * <code>
 *     class TestReceiver {
 *			[public/private/protected] void accept([Connection connection, | Session session,] TestObject testObject() {
 *			    // Handle TestObject
 *			}
 *     }
 * </code>
 *
 * For that there are some requirements:
 * <ul>
 *     <li>AnnotationProcessor to verify the receiver at compileTime</li>
 *     <li>CommunicationRegistration extension to accept and run such an receive</li>
 * </ul>
 */
@Target (ElementType.METHOD)
@Retention (RetentionPolicy.RUNTIME)
public @interface Receiver {

	boolean active() default true;

}
