package com.github.thorbenkuck.netcom2.network.shared.comm;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.exceptions.CommunicationNotSpecifiedException;
import com.github.thorbenkuck.netcom2.interfaces.Mutex;
import com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;

import java.util.List;
import java.util.Map;

/**
 * This Interface is similar to an EventBus, but more specified than an EventBus.
 * <p>
 * It composites {@link ReceivePipeline} to handle specific Objects.
 * Those can be accessed by stating:
 * <p>
 * <code>
 * CommunicationRegistration registration = ...
 * ReceivePipeline pipeline = registration.register(MyObject.class);
 * </code>
 * <p>
 * Those Pipelines handle received Objects. You may fluidly register any of the following Interfaces:
 * <p>
 * <ul>
 * <li>{@link OnReceiveSingle onReceiveSingle}</li>
 * <li>{@link OnReceive onReceive}</li>
 * <li>{@link OnReceiveTriple onReceiveTriple}</li>
 * </ul>
 * <p>
 * Its instantiation is limited to internal Developers.
 *
 * @see #trigger(Class, Connection, Session, Object)
 * @see ReceivePipeline
 */
@Synchronized
public interface CommunicationRegistration extends Mutex {

	/**
	 * Creates an instance of the CommunicationRegistration.
	 * <p>
	 * It utilizes the package-private instance, located within the same package of this interface.
	 *
	 * @return a new Instance of this interface
	 */
	@APILevel
	static CommunicationRegistration create() {
		return new DefaultCommunicationRegistration();
	}

	/**
	 * Registers and internally sets/maintains a ReceivePipeline to handle a given Object of the provided Class.
	 * <p>
	 * Because of type-erasure, the type check has to be done at runtime.
	 * You will never get an {@link ReceivePipeline receivePipeline} of another type
	 *
	 * @param clazz the Class of Object, that should be handled
	 * @param <T>   the Type of the Object, identified by the provided Class
	 * @return either a new or an already established instance of the {@link ReceivePipeline}
	 */
	<T> ReceivePipeline<T> register(final Class<T> clazz);

	/**
	 * Removes the internally set instance of the {@link ReceivePipeline}, handling an Object of the same
	 * Type as the provided <code>clazz</code>.
	 * <p>
	 * This will ignore any {@link com.github.thorbenkuck.netcom2.pipeline.ReceivePipelineHandlerPolicy} set to
	 * the maintained {@link ReceivePipeline}. It will simply remove the set instance.
	 *
	 * @param clazz the Class, that defines the type of the ReceivePipeline
	 */
	void unRegister(final Class clazz);

	/**
	 * Determines whether or not an ReceivePipeline is registered to handle Objects of the same Type as <code>clazz</code>
	 *
	 * @param clazz the Class, that defines the type of the ReceivePipeline
	 * @return true if any ReceivePipeline is set, false otherwise
	 */
	boolean isRegistered(final Class clazz);

	/**
	 * This Method is used, to trigger an Object by its declared class.
	 * <p>
	 * All it does, is to call {@link #trigger(Class, Connection, Session, Object)}, with the direct Class of the provided
	 * Object
	 *
	 * @param connection the Connection over which the Object was received
	 * @param session    the Session, identifying the other end of the Connection
	 * @param object     the Object that was received
	 * @param <T>        the Type of the OnReceivePipeline
	 * @throws CommunicationNotSpecifiedException if no ReceivePipeline is set for the provided class and no
	 *                                            DefaultCommunicationHandler is set
	 * @see #trigger(Class, Connection, Session, Object)
	 */
	@Asynchronous
	<T> void trigger(final Connection connection, final Session session, final Object object) throws CommunicationNotSpecifiedException;

	/**
	 * This Method takes an Object and runs it through an {@link ReceivePipeline} of the Type that is defined by the provided Class.
	 * <p>
	 * This Method provides the possibility to define other Class-Types than the Object is of. This is due to inheritance.
	 * If you want to run an Object through this CommunicationRegistration, but you want it to be handled by an
	 * {@link ReceivePipeline receivePipeline}, that only handles the super type or an declared/inherited interface, you may
	 * do that.
	 * <p>
	 * By calling this Method, you basically running the provided Object through the internally maintained {@link ReceivePipeline}. The other provided
	 * parameters (<code>connection</code>, <code>session</code>) will be needed regardless of the internally set handlers.
	 * This is because the ReceivePipeline does not know whether any set Handler needs those parameters or not.
	 * <p>
	 * To ensure scalability, this Method extracts the actual run of the ReceivePipeline into another Thread.
	 * Therefor this Method does not rethrow thrown Errors and RuntimeExceptions, thrown by the ReceiveHandler
	 * <p>
	 * If no ReceivePipeline was ever registered to this CommunicationRegistration, it will throw an {@link CommunicationNotSpecifiedException}.
	 * This behaviour can be overridden by providing an DefaultCommunicationHandler with {@link #addDefaultCommunicationHandler(OnReceive)}
	 *
	 * @param clazz      the Class, defining the Type of the {@link ReceivePipeline}
	 * @param connection the Connection, over which the Object has been received
	 * @param session    the Session, identifying the other end of the Connection
	 * @param o          the Object, that was received over the Connection
	 * @param <T>        the Type of the ReceivePipeline
	 * @throws CommunicationNotSpecifiedException if no ReceivePipeline is set for the provided type
	 *                                            and no DefaultCommunicationHandler has been set.
	 */
	@Asynchronous
	@SuppressWarnings ("unchecked")
	<T> void trigger(final Class<T> clazz, final Connection connection, final Session session, final Object o)
			throws CommunicationNotSpecifiedException;

	/**
	 * Sets an {@link OnReceiveSingle} as the default handling routine for any Object that is not registered specifically
	 *
	 * @param defaultCommunicationHandler the {@link OnReceiveSingle} that should be run, if no Registration was found for
	 *                                    any provided Object
	 */
	void addDefaultCommunicationHandler(final OnReceiveSingle<Object> defaultCommunicationHandler);

	/**
	 * Sets an {@link OnReceive} as the default handling routine for any Object that is not registered specifically
	 *
	 * @param defaultCommunicationHandler the {@link OnReceive} that should be run, if no Registration was found for
	 *                                    any provided Object
	 */
	void addDefaultCommunicationHandler(final OnReceive<Object> defaultCommunicationHandler);

	/**
	 * Sets an {@link OnReceiveTriple} as the default handling routine for any Object that is not registered specifically
	 *
	 * @param defaultCommunicationHandler the {@link OnReceiveTriple} that should be run, if no Registration was found for
	 *                                    any provided Object
	 */
	void addDefaultCommunicationHandler(final OnReceiveTriple<Object> defaultCommunicationHandler);

	/**
	 * Clears all Registration
	 * <p>
	 * This Call will ignore all {@link com.github.thorbenkuck.netcom2.pipeline.ReceivePipelineHandlerPolicy}.
	 * All internally set {@link ReceivePipeline} will be cleared.
	 * Currently running Handlers will NOT be stopped.
	 */
	void clear();

	/**
	 * This call wil unregister all {@link ReceivePipeline} if the {@link ReceivePipeline#isEmpty()} call returns true
	 * <p>
	 * The one Exception for this rule is, if an ReceivePipeline is sealed.
	 * Any sealed ReceivePipeline will not be tested and therefor not removed, even if they are empty.
	 * This ensures that critical empty Pipelines are not collected
	 */
	void clearAllEmptyPipelines();

	/**
	 * This call will override any internally set Registrations and set all Registration to the provided CommunicationRegistration.
	 * <p>
	 * This will ignore any {@link com.github.thorbenkuck.netcom2.pipeline.ReceivePipelineHandlerPolicy} and delete all
	 * Registrations currently set.
	 * <p>
	 * This CommunicationRegistration will take all <b>instances</b> from the provided CommunicationRegistration!
	 * This means, both are now nearly identical! A change at one {@link ReceivePipeline} in any of those CommunicationRegistrations
	 * will lead to the same change in the other CommunicationRegistration!
	 *
	 * @param communicationRegistration the CommunicationRegistration, that will provide its Registration.
	 */
	void updateBy(final CommunicationRegistration communicationRegistration);

	/**
	 * Creates a Map that maps the internally maintained {@link ReceivePipeline} to the Class they have been Registered with.
	 *
	 * @return a map, pointing from the Class-Type to the {@link ReceivePipeline} that handles this Type or any subtype
	 */
	Map<Class, ReceivePipeline<?>> map();

	/**
	 * Lists all set DefaultCommunicationHandlers
	 *
	 * @return a List with all set DefaultCommunicationHandlers
	 * @see #addDefaultCommunicationHandler(OnReceive)
	 * @see #addDefaultCommunicationHandler(OnReceiveSingle)
	 * @see #addDefaultCommunicationHandler(OnReceiveTriple)
	 */
	List<OnReceiveTriple<Object>> listDefaultsCommunicationRegistration();
}
