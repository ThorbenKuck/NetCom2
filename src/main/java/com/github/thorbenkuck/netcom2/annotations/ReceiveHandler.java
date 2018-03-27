package com.github.thorbenkuck.netcom2.annotations;

import java.lang.annotation.*;

/**
 * This annotation is used for an method, that should handle am received Object.
 * <p>
 * You register an Object as an ReceiveHandler in the following way:
 * <p>
 * <pre>
 * <code>
 * TestReceiver testReceiver = ...
 * CommunicationRegistration registration = ...
 * registration.register(TestObject.class).to(testReceiver);
 * </code>
 * </pre>
 * <p>
 * This means, that you want the <code>TestReceiver</code> class to handle received TestObjects. For that, you will have to:
 * <p>
 * <ul>
 * <li>Provide an Method, that handles the TestObject.</li>
 * <li>Optional: Also define the Session or the Connection as a parameter for this method.</li>
 * <li>Annotate this Method with {@literal @}ReceiveHandler.</li>
 * </ul>
 * <p>
 * This method:
 * <p>
 * <ul>
 * <li>Can have any visibility modifier you want (public/private/protected/package-private).</li>
 * <li>Can be called what ever you want.</li>
 * <li>Can not accept anything else than: A Connection, A Session and the Registered Object-type.</li>
 * <li>Does not have to have the Connection or the Session as an parameter.</li>
 * <li>Will not be marked as used by your ide.</li>
 * </ul>
 * <p>
 * For example, the TestReceiver might look like this:
 * <p>
 * <pre>
 *     {@code
 *     class TestReceiver {
 *         {@literal @}ReceiveHandler
 *         public void accept(Session session, TestObject testObject) {
 *             // Handle TestObject
 *         }
 *     }
 *     }
 * </pre>
 * <p>
 * With this annotation, NetCom2 might be used in the way of an EventBus.
 *
 * @version 1.0
 * @see com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline#to(Object)
 * @since 1.0
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReceiveHandler {

	/**
	 * Overriding this Method allows you to disable an ReceiveHandler without removing the Annotation.
	 * <p>
	 * If this method returns false, the {@link com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration}
	 * will not use the annotated Method, therefore ignoring the annotation and the annotated Method.
	 * <p>
	 * Further, if this Method returns false, it will not be saved and therefor not take up any resources to be saved,
	 * maintained and checked.
	 *
	 * @return boolean, whether or not the annotated class should be used to Handle Objects.
	 */
	boolean active() default true;

}
