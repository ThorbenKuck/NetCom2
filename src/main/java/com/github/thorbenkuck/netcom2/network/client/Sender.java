package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.network.shared.cache.CacheObserver;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.clients.ReceiveOrSendSynchronization;

/**
 * This interface is the bridge between Server and Client.
 * <p>
 * The {@link ClientStart} aggregates this Object and exposes it via
 * {@link ClientStart#send()}.
 * <p>
 * It can be used to:
 * <ul>
 * <li>send Objects to the Server, so that the {@link com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration} of the Server handles it</li>
 * <li>register to a certain type of message</li>
 * <li>unregister from a certain type of message</li>
 * </ul>
 * <p>
 * The reset Method should not be used within production. Reset clears out all saved instances of requested registrations
 * and therefore is after calling it not able to resolve register responses.
 *
 * @version 1.0
 * @since 1.0
 */
public interface Sender {

	/**
	 * Sends an Object over the network to the server.
	 * <p>
	 * The call will result in an asynchronous extraction. However, it returns a synchronization mechanism,
	 * that will allow the developer to listen for a successful send or incoming messages.
	 * <p>
	 * It uses the {@link com.github.thorbenkuck.netcom2.network.shared.clients.DefaultConnection}
	 *
	 * @param o the Object that should be send over the network.
	 * @return a synchronization mechanism to allow procedural programming
	 */
	ReceiveOrSendSynchronization objectToServer(Object o);

	/**
	 * Sends an Object over the network to the server.
	 * <p>
	 * The call will result in an asynchronous extraction. However, it returns a synchronization mechanism,
	 * that will allow the developer to listen for a successful send or incoming messages.
	 * <p>
	 * It uses the specified Connection.
	 *
	 * @param o          the Object that should be send over the network.
	 * @param connection the Connection that should be used.
	 * @return an synchronization mechanism to allow procedural programming
	 */
	ReceiveOrSendSynchronization objectToServer(Object o, Connection connection);

	/**
	 * Sends an Object over the network to the server.
	 * <p>
	 * The call will result in an asynchronous extraction. However, it returns a synchronization mechanism,
	 * that will allow the developer to listen for a successful send or incoming messages.
	 * <p>
	 * It uses the Connection, identified by the provided key.
	 *
	 * @param o             the Object that should be send over the network.
	 * @param connectionKey the identifier of the Connection that should be used
	 * @return an synchronization mechanism to allow procedural programming
	 */
	ReceiveOrSendSynchronization objectToServer(Object o, Class connectionKey);

	/**
	 * Sends a register request to the Server. If successful, the Server will update the Client every time, the requested
	 * Object is updated
	 * <p>
	 * The call will result in an asynchronous extraction. However, it returns a synchronization mechanism,
	 * that will allow the developer to listen for a successful send or incoming messages.
	 *
	 * @param clazz    the class of the message, you want to register to
	 * @param observer the callback object, that should be called, if an Object of the specified type arrives
	 * @param <T>      the type of the message, you want to register to
	 * @return a synchronization mechanism
	 */
	<T> ReceiveOrSendSynchronization registrationToServer(Class<T> clazz, CacheObserver<T> observer);

	/**
	 * Sends a register request to the Server. If successful, the Server will update the Client every time, the requested
	 * Object is updated
	 * <p>
	 * The call will result in an asynchronous extraction. However, it returns a synchronization mechanism,
	 * that will allow the developer to listen for a successful send or incoming messages.
	 * <p>
	 * Uses the provided Connection.
	 *
	 * @param clazz      the class of the message, you want to register to
	 * @param observer   the callback object, that should be called, if an Object of the specified type arrives
	 * @param <T>        the type of the message, you want to register to
	 * @param connection the Connection that should be used.
	 * @return a synchronization mechanism
	 */
	<T> ReceiveOrSendSynchronization registrationToServer(Class<T> clazz, CacheObserver<T> observer,
	                                                      Connection connection);

	/**
	 * Sends a register request to the Server. If successful, the Server will update the Client every time, the requested
	 * Object is updated
	 * <p>
	 * The call will result in an asynchronous extraction. However, it returns an synchronization mechanism,
	 * that will allow te developer to listen for a successful send or incoming messages.
	 * <p>
	 * Uses the Connection, identified with the provided <code>connectionKey</code>
	 *
	 * @param clazz         the class of the message, you want to register to
	 * @param observer      the callback object, that should be called, if an Object of the specified type arrives
	 * @param <T>           the type of the message, you want to register to
	 * @param connectionKey the key for the Connection that should be used.
	 * @return a synchronization mechanism
	 */
	<T> ReceiveOrSendSynchronization registrationToServer(Class<T> clazz, CacheObserver<T> observer,
	                                                      Class connectionKey);

	/**
	 * Requests an unRegistration from the specified message type.
	 * <p>
	 * The call will result in an asynchronous extraction. However, it returns an synchronization mechanism,
	 * that will allow te developer to listen for a successful send or incoming messages.
	 *
	 * @param clazz the class of the message, you want to register to
	 * @param <T>   the type of the message, you want to register to
	 * @return a synchronization mechanism
	 */
	<T> ReceiveOrSendSynchronization unRegistrationToServer(Class<T> clazz);

	/**
	 * Requests an unRegistration from the specified message type.
	 * <p>
	 * The call will result in an asynchronous extraction. However, it returns an synchronization mechanism,
	 * that will allow the developer to listen for a successful send or incoming messages.
	 *
	 * @param clazz      the class of the message, you want to register to
	 * @param connection the Connection that should be used
	 * @param <T>        the type of the message, you want to register to
	 * @return a synchronization mechanism
	 */
	<T> ReceiveOrSendSynchronization unRegistrationToServer(Class<T> clazz, Connection connection);

	/**
	 * Requests an unRegistration from the specified message type.
	 * <p>
	 * The call will result in an asynchronous extraction. However, it returns an synchronization mechanism,
	 * that will allow the developer to listen for a successful send or incoming messages.
	 *
	 * @param clazz         the class of the message, you want to register to
	 * @param <T>           the type of the message, you want to register to
	 * @param connectionKey the class, identifying the Connection that should be used.
	 * @return a synchronization mechanism
	 */
	<T> ReceiveOrSendSynchronization unRegistrationToServer(Class<T> clazz, Class connectionKey);

	/**
	 * Resets the Sender to its original state.
	 * <p>
	 * Calling this Method is not recommended. It is utilized, to reset a Client, once it disconnects from the Server,
	 * to allow it to reconnect. It cleans out all internal saved instances
	 * <p>
	 * Calling this Method results in a clean of all requested registration and there corresponding callback Objects.
	 * So if you send an register Request and shortly after call this method, the callback Object will no longer be available.
	 * <p>
	 * This method is called if the last Connection is disconnected.
	 */
	void reset();

}
