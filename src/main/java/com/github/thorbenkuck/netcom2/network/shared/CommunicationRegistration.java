package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.keller.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.exceptions.CommunicationNotSpecifiedException;
import com.github.thorbenkuck.netcom2.interfaces.Mutex;
import com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.network.shared.connections.Connection;
import com.github.thorbenkuck.netcom2.network.shared.connections.ConnectionContext;

import java.util.List;
import java.util.Map;

public interface CommunicationRegistration extends Mutex {

	static CommunicationRegistration open() {
		return new NativeCommunicationRegistration();
	}

	/**
	 * Sets/maintains a ReceivePipeline to handle a given Object of the provided Class.
	 * <p>
	 * Because of type-erasure, the type check has to be done at runtime.
	 * You will never get an {@link ReceivePipeline receivePipeline} of another type
	 *
	 * @param clazz the Class of the Object, that should be handled.
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
	 * Determines whether or not a {@link ReceivePipeline} is registered to handle Objects of the same Type as <code>clazz</code>
	 *
	 * @param clazz the Class, that defines the type of the {@link ReceivePipeline}
	 * @return true if any {@link ReceivePipeline} is set, false otherwise
	 */
	boolean isRegistered(final Class clazz);

	/**
	 * This method is used to trigger a {@link ReceivePipeline}, with the provided arguments.
	 * <p>
	 * All it does, is to call {@link #trigger(Class, Connection, Session, Object)}, with the direct Class of the provided
	 * Object
	 *
	 * Since the ConnectionContext has been introduced to provide a couple between the Connection and the Client, this
	 * method is a potential candidate for deprecated.
	 *
	 * @param connection the {@link Connection} over which the Object was received
	 * @param session    the {@link Session}, identifying the other end of the {@link Connection}
	 * @param object     the Object that was received
	 * @throws CommunicationNotSpecifiedException if no ReceivePipeline is set for the provided class and no
	 *                                            DefaultCommunicationHandler is set
	 * @see #trigger(Class, Connection, Session, Object)
	 */
	@Asynchronous
	@Deprecated
	default void trigger(final Connection connection, final Session session, final Object object) throws CommunicationNotSpecifiedException {
		trigger(connection.context(), session, object);
	}

	/**
	 * This method takes a Object and runs it through a {@link ReceivePipeline} of the Type that is defined by the provided Class.
	 * <p>
	 * This method provides the possibility to define other Class-Types than the Object is of. This is due to inheritance.
	 * If you want to run an Object through this CommunicationRegistration, but you want it to be handled by an
	 * {@link ReceivePipeline}, that only handles the super type or an declared/inherited interface, you may
	 * do that.
	 * <p>
	 * By calling this Method, you  are basically running the provided Object through the internally maintained {@link ReceivePipeline}.
	 * The other provided parameters (<code>connection</code>, <code>session</code>) will be needed regardless of the
	 * internally set handlers. This is because the ReceivePipeline does not know whether any set Handler needs those
	 * parameters or not. It only knows {@link OnReceiveTriple}
	 * <p>
	 * To ensure scalability, this Method extracts the actual run of the {@link ReceivePipeline} into another Thread.
	 * Therefor this method does not rethrow thrown Errors, Exceptions and RuntimeExceptions, thrown by the ReceiveHandler.
	 * <p>
	 * If no {@link ReceivePipeline} was ever registered to this CommunicationRegistration, it will throw an
	 * {@link CommunicationNotSpecifiedException}. his behaviour can be overridden by providing an DefaultCommunicationHandler
	 * with {@link #addDefaultCommunicationHandler(OnReceive)}
	 *
	 * Since the ConnectionContext has been introduced to provide a couple between the Connection and the Client, this
	 * method is a potential candidate for deprecated.
	 *
	 * @param clazz      the Class, defining the Type of the {@link ReceivePipeline}
	 * @param connection the {@link Connection}, over which the Object has been received
	 * @param session    the {@link Session}, identifying the other end of the {@link Connection}
	 * @param o          the Object, that was received over the {@link Connection}
	 * @param <T>        the Type of the {@link ReceivePipeline}
	 * @throws CommunicationNotSpecifiedException if no {@link ReceivePipeline} is set for the provided type
	 *                                            and no DefaultCommunicationHandler has been set.
	 * @see OnReceiveTriple
	 * @see com.github.thorbenkuck.netcom2.pipeline.Wrapper
	 */
	@Asynchronous
	@Deprecated
	@SuppressWarnings("unchecked")
	default <T> void trigger(final Class<T> clazz, final Connection connection, final Session session, final Object o)
			throws CommunicationNotSpecifiedException {
		trigger(clazz, connection.context(), session, o);
	}

	void trigger(ConnectionContext connectionContext, Session session, Object object) throws CommunicationNotSpecifiedException;

	@Asynchronous
	<T> void trigger(Class<T> clazz, ConnectionContext connectionContext, Session session, Object o)
			throws CommunicationNotSpecifiedException;

	/**
	 * Sets an {@link OnReceiveSingle} as the default handling routine for any Object that is not registered specifically
	 *
	 * @param defaultCommunicationHandler the {@link OnReceiveSingle} that should be run, if no registration was found for
	 *                                    a specific provided Object
	 */
	void addDefaultCommunicationHandler(final OnReceiveSingle<Object> defaultCommunicationHandler);

	/**
	 * Sets an {@link OnReceive} as the default handling routine for any Object that is not registered specifically
	 *
	 * @param defaultCommunicationHandler the {@link OnReceive} that should be run, if no registration was found for
	 *                                    a specific provided Object
	 */
	void addDefaultCommunicationHandler(final OnReceive<Object> defaultCommunicationHandler);

	/**
	 * Sets an {@link OnReceiveTriple} as the default handling routine for any Object that is not registered specifically
	 *
	 * @param defaultCommunicationHandler the {@link OnReceiveTriple} that should be run, if no registration was found for
	 *                                    a specific provided Object
	 */
	void addDefaultCommunicationHandler(final OnReceiveTriple<Object> defaultCommunicationHandler);

	/**
	 * Clears all set registration.
	 * <p>
	 * This Call will ignore the set {@link com.github.thorbenkuck.netcom2.pipeline.ReceivePipelineHandlerPolicy}.
	 * <p>
	 * All internally set {@link ReceivePipeline} will be cleared.
	 * Currently running Handlers will NOT be stopped.
	 */
	void clear();

	/**
	 * This call wil unregister all {@link ReceivePipeline} if the {@link ReceivePipeline#isEmpty()} call returns true.
	 * <p>
	 * The one exception for this rule is, if an {@link ReceivePipeline} is sealed.
	 * Any sealed {@link ReceivePipeline} will not be tested and therefor not removed, even if they are empty.
	 * This ensures that critical empty {@link ReceivePipeline} are not collected.
	 */
	void clearAllEmptyPipelines();

	/**
	 * This call will override any internally set registrations and set all registration to the provided CommunicationRegistration.
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
