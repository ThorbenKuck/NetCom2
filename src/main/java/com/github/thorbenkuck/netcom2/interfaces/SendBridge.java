package com.github.thorbenkuck.netcom2.interfaces;

import com.github.thorbenkuck.netcom2.network.shared.Session;

/**
 * This Class is used to abstract away the Send-mechanism.
 * <p>
 * It decouples the sender and the sending {@link Session}, whilst still
 * maintaining the possibility to send something over the {@link com.github.thorbenkuck.netcom2.network.shared.connections.DefaultConnection}.
 * <p>
 * With that you can change the target of sending using the strategy pattern. By providing an Setter for the SendBridge
 * the sender may be changed at runtime.
 *
 * @version 1.0
 * @see Session#send(Object)
 * @see com.github.thorbenkuck.netcom2.network.shared.clients.Client#send(Object)
 * @since 1.0
 */
@FunctionalInterface
public interface SendBridge {

	/**
	 * This call will send the Object o over the {@link com.github.thorbenkuck.netcom2.network.shared.connections.DefaultConnection}
	 * of the internally registered Sender.
	 *
	 * @param o the Object, that should be send.
	 */
	void send(final Object o);

}
