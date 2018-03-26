package com.github.thorbenkuck.netcom2.network.shared.clients;

/**
 * This interface allows you, to wait for a certain object to be send or received.
 *
 * @version 1.0
 * @since 1.0
 */
public interface ReceiveOrSendSynchronization {

	/**
	 * With calling this Method, the ReceivingService will receive an Callback, that listens for the specified Object.
	 *
	 * This call will block, until the required class is received.
	 *
	 * @param clazz the type, that should be received.
	 * @throws InterruptedException if the Thread is interrupted, while waiting for the receiving of this Object
	 */
	void andWaitForReceiving(final Class clazz) throws InterruptedException;

	/**
	 * With calling this Method, the SendingService will receive an Callback, that will block until the Object, you just send
	 * is successfully send over the Connection.
	 *
	 * This call will block, until the required object is send.
	 *
	 * @throws InterruptedException if the Thread is interrupted, while waiting for the receiving of this Object
	 */
	void andWaitForSendFinished() throws InterruptedException;

}
