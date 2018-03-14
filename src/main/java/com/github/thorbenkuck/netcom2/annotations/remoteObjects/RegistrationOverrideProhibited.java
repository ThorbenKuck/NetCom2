package com.github.thorbenkuck.netcom2.annotations.remoteObjects;

import com.github.thorbenkuck.netcom2.interfaces.RemoteObjectRegistration;

import java.lang.annotation.*;

/**
 * This annotation signals to the {@link RemoteObjectRegistration}, that it
 * should not be overridden if it is already registered.
 * <p>
 * If the {@link RemoteObjectRegistration} contains any instance of the provided
 * annotated Type, it will not be overridden. So the following will lead to the first object staying within the registration
 * <p>
 * <code>
 * \\@RegistrationOverrideProhibited
 * interface Test {
 * void test();
 * }
 *
 * class Testing {
 *
 * RemoteObjectRegistration registration = ...
 *
 * void run() {
 * // This will register the TestImpl Class to the Registration, identified by the Test.class
 * registration.register(new TestImpl(), Test.class);
 * // This call will not work. It will debug, that it could not be registered
 * registration.register(new TestImpl(), Test.class);
 * // Same as above. However, TestImpl will still be registered to TestImpl.class
 * registration.hook(new TestImpl());
 * }
 *
 * private class TestImpl implements Test {
 * \\@Override
 * public void test() {
 * // Do Something
 * }
 * }
 * }
 * </code>
 * <p>
 * So you may say that, any Object may be set to the RemoteObjectRegistration, but not updated.
 * <p>
 * Note: If you call {@link RemoteObjectRegistration#clear()}, this instance will still be cleared from the Registration
 */
@Documented
@Inherited
@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.TYPE)
public @interface RegistrationOverrideProhibited {
}
