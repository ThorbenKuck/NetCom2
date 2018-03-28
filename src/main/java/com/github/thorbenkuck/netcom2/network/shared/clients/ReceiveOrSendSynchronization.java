package com.github.thorbenkuck.netcom2.network.shared.clients;

/**
 * This interface allows you, to wait for a certain Object to be send or received over the previously requested {@link Connection}.
 *
 * @version 1.0
 * @since 1.0
 * @see Connection#write(Object)
 */
public interface ReceiveOrSendSynchronization {

	/**
	 * With calling this method, a {@link com.github.thorbenkuck.netcom2.network.shared.Callback} will be added to the
	 * {@link com.github.thorbenkuck.netcom2.network.interfaces.ReceivingService}, which listens for the specified Object-type.
	 * <p>
	 * This call will block, until the required class is received.
	 *
	 * @param clazz the type, that should be received.
	 * @throws InterruptedException if the Thread is interrupted, while waiting for the receiving of this Object.
	 * @see com.github.thorbenkuck.netcom2.network.interfaces.ReceivingService#addReceivingCallback(com.github.thorbenkuck.netcom2.network.shared.Callback)
	 */
	void andWaitForReceiving(final Class clazz) throws InterruptedException;

	/**
	 * With calling this method, a {@link com.github.thorbenkuck.netcom2.network.shared.Callback} will be added to the
	 * {@link com.github.thorbenkuck.netcom2.network.interfaces.SendingService}, which will block until the Object, you
	 * just send is successfully written to the {@link Connection}.
	 * <p>
	 * This call will block, until the required Object is send.
	 *
	 * @throws InterruptedException if the Thread is interrupted, while waiting for the receiving of this Object
	 */
	void andWaitForSendFinished() throws InterruptedException;

}
