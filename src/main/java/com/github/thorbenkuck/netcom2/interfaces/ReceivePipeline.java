package com.github.thorbenkuck.netcom2.interfaces;

import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.pipeline.ReceivePipelineCondition;
import com.github.thorbenkuck.netcom2.pipeline.ReceivePipelineHandlerPolicy;

import java.util.function.Consumer;

/**
 * A ReceivePipeline differs from the {@link com.github.thorbenkuck.keller.pipe.Pipeline}, by the Class,
 * which they hold.
 * <p>
 * It holds the OnReceive-Family instead of the {@link Consumer}. With method-overloading, you may provide any of the
 * OnReceive-interfaces, without changing the code massively. This is, if you write it as an Lambda or a Method reference.
 * <p>
 * So, for example, if you have the following code:
 * <p>
 * <pre>
 *     {@code
 * public void register(CommunicationRegistration registration) {
 * registration.register(TestObject.class)
 *        .addFirst(this::handle);
 * }
 *
 * private void handle(TestObject testObject) {
 *     // do something with the test Object
 *     ...
 * }
 *     }
 * </pre>
 * <p>
 * You can change it easily by simple changing the handle method like this:
 * <p>
 * <pre><code>
 * private void handle(Session session, TestObject testObject) {
 *     // do something with the test Object AND the Session
 *     ...
 * }
 * </code></pre>
 * <p>
 * or, if you need the Connection:
 * <p>
 * <pre><code>
 * private void handle(Connection connection, Session session, TestObject testObject) {
 *     // do something with the test Object, the Session AND the Connection
 *     ...
 * }
 * </code></pre>
 * <p>
 * You do not need to change the register Method with this. The only important thing is, that the sequence of those 3 Objects
 * is not changed. So first the Connection, than the Session, than the Object. You may leave out any Number of parameters,
 * starting at the left side.
 * <p>
 * The massive amount of Methods is simply for convenience sake.
 *
 * @param <T> T tye Type that should be handled by this ReceivePipeline
 * @version 1.0
 * @since 1.0
 */
public interface ReceivePipeline<T> extends Mutex {
	/**
	 * This Method adds an pipelineService to the tail of this ReceivePipeline
	 * <p>
	 * Accepts an {@link OnReceive} interface, to add an handler to this ReceivePipeline.
	 * <p>
	 * Upon calling this Method, this ReceivePipeline, will handle and processes the OnReceive and than links it to an
	 * {@link ReceivePipelineCondition}, which is than returned and may be manipulated by the developer.
	 * <p>
	 * This {@link ReceivePipelineCondition} is responsible for defining whether or not the so added {@link OnReceive}
	 * is executed upon calling the {@link #run(Connection, Session, Object)}.
	 * <p>
	 * Every {@link OnReceive} encapsulated this way has its own ReceivePipelineCondition. You may add any number of
	 * {@link java.util.function.Predicate} to it.
	 *
	 * @param pipelineService th {@link OnReceive}, which is added to this ReceivePipeline to handle the defined Object
	 * @return an {@link ReceivePipelineCondition}, to describe whether or not the pipelineService is executed upon calling {@link #run(Connection, Session, Object)}
	 */
	ReceivePipelineCondition<T> addLast(final OnReceive<T> pipelineService);

	/**
	 * This Method adds an pipelineService to the tail of this ReceivePipeline
	 * <p>
	 * Uses the {@link OnReceiveSingle} instead of the {@link OnReceive} interface.
	 *
	 * @param pipelineService the {@link OnReceiveSingle}, which is added to this ReceivePipeline to handle the defined Object
	 * @return an {@link ReceivePipelineCondition}, to describe whether or not the pipelineService is executed upon calling {@link #run(Connection, Session, Object)}
	 * @see #addLast(OnReceive)
	 */
	ReceivePipelineCondition<T> addLast(final OnReceiveSingle<T> pipelineService);

	/**
	 * This Method adds an pipelineService to the tail of this ReceivePipeline
	 * <p>
	 * Uses the {@link OnReceiveTriple} instead of the {@link OnReceive} interface
	 *
	 * @param pipelineService the {@link OnReceiveTriple}, which is added to this ReceivePipeline to handle the defined Object
	 * @return an {@link ReceivePipelineCondition}, to describe whether or not the pipelineService is executed upon calling {@link #run(Connection, Session, Object)}
	 * @see #addLast(OnReceive)
	 */
	ReceivePipelineCondition<T> addLast(final OnReceiveTriple<T> pipelineService);

	/**
	 * This Method adds an pipelineService to the head of this ReceivePipeline
	 * <p>
	 * Accepts an {@link OnReceive} interface, to add an handler to this ReceivePipeline.
	 * <p>
	 * Upon calling this Method, this ReceivePipeline, will handle and processes the OnReceive and than links it to an
	 * {@link ReceivePipelineCondition}, which is than returned and may be manipulated by the developer.
	 * <p>
	 * This {@link ReceivePipelineCondition} is responsible for defining whether or not the so added {@link OnReceive}
	 * is executed upon calling the {@link #run(Connection, Session, Object)}.
	 * <p>
	 * Every {@link OnReceive} encapsulated this way has its own ReceivePipelineCondition. You may add any number of
	 * {@link java.util.function.Predicate} to it.
	 *
	 * @param pipelineService the {@link OnReceive}, which is added to the head of this ReceivePipeline to handle the defined Object
	 * @return an {@link ReceivePipelineCondition}, to describe whether or not the pipelineService is executed upon calling {@link #run(Connection, Session, Object)}
	 */
	ReceivePipelineCondition<T> addFirst(final OnReceive<T> pipelineService);

	/**
	 * This Method adds an pipelineService to the head of this ReceivePipeline
	 * <p>
	 * Uses the {@link OnReceiveSingle} instead of the {@link OnReceive} interface
	 *
	 * @param pipelineService the {@link OnReceiveSingle}, which is added to the head of this ReceivePipeline to handle the defined Object
	 * @return an {@link ReceivePipelineCondition}, to describe whether or not the pipelineService is executed upon calling {@link #run(Connection, Session, Object)}
	 * @see #addFirst(OnReceive)
	 */
	ReceivePipelineCondition<T> addFirst(final OnReceiveSingle<T> pipelineService);

	/**
	 * This Method adds an pipelineService to the head of this ReceivePipeline
	 * <p>
	 * Uses the {@link OnReceiveTriple} instead of the {@link OnReceive} interface
	 *
	 * @param pipelineService the {@link OnReceiveTriple}, which is added to the head of this ReceivePipeline to handle the defined Object
	 * @return an {@link ReceivePipelineCondition}, to describe whether or not the pipelineService is executed upon calling {@link #run(Connection, Session, Object)}
	 * @see #addFirst(OnReceive)
	 */
	ReceivePipelineCondition<T> addFirst(final OnReceiveTriple<T> pipelineService);

	/**
	 * Adds an {@link OnReceive} to the head of this ReceivePipeline, if it isn't already set.
	 * <p>
	 * Otherwise, it has the same behaviour as {@link #addFirst(OnReceive)}
	 *
	 * @param pipelineService the {@link OnReceive}, which is added to the head of this ReceivePipeline if it isn't already contained with in it, to handle the defined Object
	 * @return an {@link ReceivePipelineCondition}, to describe whether or not the pipelineService is executed upon calling {@link #run(Connection, Session, Object)}
	 * @see #addFirst(OnReceive)
	 */
	ReceivePipelineCondition<T> addFirstIfNotContained(final OnReceive<T> pipelineService);

	/**
	 * Adds an {@link OnReceiveSingle} to the head of this ReceivePipeline, if it isn't already set.
	 * <p>
	 * Otherwise, it has the same behaviour as {@link #addFirst(OnReceiveSingle)}
	 *
	 * @param pipelineService the {@link OnReceiveSingle}, which is added to the head of this ReceivePipeline if it isn't already contained with in it, to handle the defined Object
	 * @return an {@link ReceivePipelineCondition}, to describe whether or not the pipelineService is executed upon calling {@link #run(Connection, Session, Object)}
	 * @see #addFirst(OnReceiveSingle)
	 */
	ReceivePipelineCondition<T> addFirstIfNotContained(final OnReceiveSingle<T> pipelineService);

	/**
	 * Adds an {@link OnReceiveTriple} to the head of this ReceivePipeline, if it isn't already set.
	 * <p>
	 * Otherwise, it has the same behaviour as {@link #addFirst(OnReceiveTriple)}
	 *
	 * @param pipelineService th {@link OnReceiveTriple}, which is added to the head of this ReceivePipeline if it isn't already contained with in it, to handle the defined Object
	 * @return an {@link ReceivePipelineCondition}, to describe whether or not the pipelineService is executed upon calling {@link #run(Connection, Session, Object)}
	 * @see #addFirst(OnReceiveTriple)
	 */
	ReceivePipelineCondition<T> addFirstIfNotContained(final OnReceiveTriple<T> pipelineService);

	/**
	 * Adds an {@link OnReceive} to the Tail of this ReceivePipeline, if it isn't already set.
	 * <p>
	 * Otherwise, it has the same behaviour as {@link #addLast(OnReceive)}
	 *
	 * @param pipelineService th {@link OnReceive}, which is added to the head of this ReceivePipeline if it isn't already contained with in it, to handle the defined Object
	 * @return an {@link ReceivePipelineCondition}, to describe whether or not the pipelineService is executed upon calling {@link #run(Connection, Session, Object)}
	 * @see #addLast(OnReceive)
	 */
	ReceivePipelineCondition<T> addLastIfNotContained(final OnReceive<T> pipelineService);

	/**
	 * Adds an {@link OnReceiveSingle} to the Tail of this ReceivePipeline, if it isn't already set.
	 * <p>
	 * Otherwise, it has the same behaviour as {@link #addLast(OnReceiveSingle)}
	 *
	 * @param pipelineService th {@link OnReceiveSingle}, which is added to the head of this ReceivePipeline if it isn't already contained with in it, to handle the defined Object
	 * @return an {@link ReceivePipelineCondition}, to describe whether or not the pipelineService is executed upon calling {@link #run(Connection, Session, Object)}
	 * @see #addLast(OnReceiveSingle)
	 */
	ReceivePipelineCondition<T> addLastIfNotContained(final OnReceiveSingle<T> pipelineService);

	/**
	 * Adds an {@link OnReceiveTriple} to the Tail of this ReceivePipeline, if it isn't already set.
	 * <p>
	 * Otherwise, it has the same behaviour as {@link #addLast(OnReceiveTriple)}
	 *
	 * @param pipelineService th {@link OnReceiveTriple}, which is added to the head of this ReceivePipeline if it isn't already contained with in it, to handle the defined Object
	 * @return an {@link ReceivePipelineCondition}, to describe whether or not the pipelineService is executed upon calling {@link #run(Connection, Session, Object)}
	 * @see #addLast(OnReceiveTriple)
	 */
	ReceivePipelineCondition<T> addLastIfNotContained(final OnReceiveTriple<T> pipelineService);

	/**
	 * Links an real Method to handle the object (and the Session, Connection), if it arrives.
	 * <p>
	 * The behaviour up on calling this Method is described by the internal {@link ReceivePipelineHandlerPolicy}, which
	 * can be set by the {@link #setReceivePipelineHandlerPolicy(ReceivePipelineHandlerPolicy)}.
	 * <p>
	 * The registered Method may accept any of the following Object:
	 * <ul>
	 * <li>T, the object which is received over the Connection</li>
	 * <li>The Session, responsible for the Client, which send the Object</li>
	 * <li>The Connection over which the Object T has been received.</li>
	 * </ul>
	 * <p>
	 * The sequence of the declared Methods does not matter, you may declare the Object first, followed by the Session,
	 * or only the Connection.
	 * <p>
	 * For correctly detecting the Method, the ReceivePipeline requires you to add the @ReceiveHandler Annotation to the
	 * Method, which should handle the receiving of the Object
	 * <p>
	 * It looked something like this:
	 * <p>
	 * <pre><code>
	 * class TestObjectHandler {
	 *     {@literal @}ReceiveHandler
	 *     private void handle(TestObject testObject, Session session) {
	 *         // Do something
	 *     }
	 * }
	 *
	 * class Register {
	 *     private CommunicationRegistration registration;
	 *     private TestObjectHandler handler = new TestObjectHandler();
	 *     ...
	 *
	 *     public void registerObjects() {
	 *         communicationRegistration.register(TestObject.class)
	 *                   .to(handler);
	 *     }
	 * }
	 * </code></pre>
	 * <p>
	 * Note that the {@literal @}ReceiveHandler Annotation is commented out to ensure the correct JavaDOC parsing.
	 * <p>
	 * WARNING: This methods needs to use Reflections to find the correct Method!
	 *
	 * @param object the Object, which contains the Method to handle the receiving of the Object, which is annotated with @ReceiveHandler
	 * @return and {@link ReceivePipelineCondition} to specify if the linked Method is executed
	 */
	ReceivePipelineCondition<T> to(final Object object);

	/**
	 * Describes if an specific {@link OnReceiveTriple} is already registered withing this ReceivePipeline
	 *
	 * @param onReceiveTriple the {@link OnReceiveTriple} which should be checked for
	 * @return true, if the provided instance is registered within this ReceivePipeline, else false
	 */
	boolean contains(final OnReceiveTriple<T> onReceiveTriple);

	/**
	 * Describes if an specific {@link OnReceive} is already registered withing this ReceivePipeline
	 *
	 * @param onReceive the {@link OnReceive} which should be checked for
	 * @return true, if the provided instance is registered within this ReceivePipeline, else false
	 */
	boolean contains(final OnReceive<T> onReceive);

	/**
	 * Describes if an specific {@link OnReceiveSingle} is already registered withing this ReceivePipeline
	 *
	 * @param onReceiveSingle the {@link OnReceiveSingle} which should be checked for
	 * @return true, if the provided instance is registered within this ReceivePipeline, else false
	 */
	boolean contains(final OnReceiveSingle<T> onReceiveSingle);

	/**
	 * Describes, whether or not this ReceivePipeline is sealed or not.
	 * <p>
	 * By default an ReceivePipeline is not sealed
	 *
	 * @return true, if this ReceivePipeline is sealed using the method {@link #seal()}, else false
	 */
	boolean isSealed();

	/**
	 * Describes, whether of not this ReceivePipeline is closed or not.
	 * <p>
	 * By default an ReceivePipeline is not closed
	 *
	 * @return true, if the ReceivePipeline is sealed using the method {@link #close()}, else false
	 */
	boolean isClosed();

	/**
	 * Describes whether or not this ReceivePipeline is empty
	 * <p>
	 * If either no instance of the OnReceive-Family has been added to this ReceivePipeline or the Method {@link #clear()}
	 * has been called, this Method returns true.
	 * <p>
	 * By default an ReceivePipeline is empty
	 *
	 * @return true, whether or not this ReceivePipeline is empty or not
	 */
	boolean isEmpty();

	/**
	 * Executes a provided {@link Consumer} over this ReceivePipeline, if it is closed.
	 * <p>
	 * It tries to execute the parameter directly and does not store it.
	 *
	 * @param consumer the {@link Consumer}, which should be executed if the ReceivePipeline is closed
	 */
	void ifClosed(final Consumer<ReceivePipeline<T>> consumer);

	/**
	 * Executes a provided {@link Runnable} if it is closed.
	 * <p>
	 * It tries to execute the parameter directly and does not store it.
	 *
	 * @param runnable the {@link Runnable}, which should be executed if the ReceivePipeline is closed
	 */
	void ifClosed(final Runnable runnable);

	/**
	 * Updates the {@link ReceivePipelineHandlerPolicy} of this ReceivePipeline.
	 * <p>
	 * The given {@link ReceivePipelineHandlerPolicy} describes the behaviour of the Method {@link #to(Object)}, whenever
	 * an Object-Handler is registered.
	 * <p>
	 * The new {@link ReceivePipelineHandlerPolicy} takes affect at the next call of {@link #to(Object)}
	 *
	 * @param receivePipelineHandlerPolicy the {@link ReceivePipelineHandlerPolicy} to be used within this ReceivePipeline
	 */
	void setReceivePipelineHandlerPolicy(final ReceivePipelineHandlerPolicy receivePipelineHandlerPolicy);

	/**
	 * Removes a certain {@link OnReceive} from this ReceivePipeline.
	 * <p>
	 * This Method is very work intensive, since it has to search every single registered element and compare each of
	 * the so registered {@link OnReceive}, {@link OnReceiveSingle} and {@link OnReceiveTriple}. It would be faster to
	 * clear this ReceivePipeline and redeclare each other OnReceive-Handler
	 *
	 * @param pipelineService the {@link OnReceive} which was added to this ReceivePipeline and now should be removed
	 */
	void remove(final OnReceive<T> pipelineService);

	/**
	 * Removes all registered OnReceive-Family from this ReceivePipeline.
	 * <p>
	 * After this method is finished, {@link #isEmpty()} will return true
	 */
	void clear();

	/**
	 * Runs a certain T through this ReceivePipeline.
	 * <p>
	 * It will check every {@link ReceivePipelineCondition}, to see whether or not the so registered OnReceive will be executed
	 *
	 * @param connection the {@link Connection}, which is associated with the receiving of the T
	 * @param session    the {@link Session}, which is associated with the receiving of the T
	 * @param t          the Object, which should be run through this ReceivePipeline
	 */
	void run(final Connection connection, final Session session, final T t);

	/**
	 * Closes this ReceivePipeline and stops all Additions to it.
	 * <p>
	 * If this Method is called {@link #isClosed()} will return true and every call of addFirst or addLast will only trigger
	 * the {@link OnReceive#onAddFailed()} method, which might be overridden by the developer
	 */
	void close();

	/**
	 * <p>
	 * Sets the ReceivePipeline to an unchangeable open-state. If you close and than seal it, it cannot be opened any more.
	 * This seal is permanent and makes the ReceivePipeline immutable.
	 * </p>
	 * <p>
	 * <b>Note:</b>  If you seal an Pipeline, it will not get collected, by a {@link CommunicationRegistration#clearAllEmptyPipelines()}
	 * call. How ever, a {@link CommunicationRegistration#unRegister(Class)} call will still remove the Pipeline
	 * </p>
	 */
	void seal();

	/**
	 * Inverts the {@link #close()} call.
	 * <p>
	 * If this method is called {@link #isClosed()} will return false and every call of addFirst of addLast will complete
	 * correctly.
	 */
	void open();

	/**
	 * Returns the number of handlers contained within the ReceivePipeline.
	 *
	 * @return the count of all ReceivePipelineHandler
	 */
	int size();
}
