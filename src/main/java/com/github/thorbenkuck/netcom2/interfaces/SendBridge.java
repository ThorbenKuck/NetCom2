package com.github.thorbenkuck.netcom2.interfaces;

/**
 * This Class is used to abstract the Send-mechanism.
 * <p>
 * It decouples the sender and the sending person, whilst still maintaining the possibility to send something over the
 * {@link com.github.thorbenkuck.netcom2.network.shared.clients.DefaultConnection}.
 * <p>
 * With that you can change the target of sending using the strategy pattern. By providing an Setter for the SendBridge
 * the sender might be changed at runtime.
 */
@FunctionalInterface
public interface SendBridge {

	/**
	 * This call will send the Object o over the {@link com.github.thorbenkuck.netcom2.network.shared.clients.DefaultConnection}
	 * of the internally registered Sender.
	 *
	 * @param o the Object, that should be send.
	 */
	void send(final Object o);

}
