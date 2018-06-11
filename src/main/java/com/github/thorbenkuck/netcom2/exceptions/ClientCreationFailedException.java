package com.github.thorbenkuck.netcom2.exceptions;

import com.github.thorbenkuck.netcom2.network.shared.session.Session;

/**
 * This Exception is thrown, if the {@link com.github.thorbenkuck.netcom2.network.shared.clients.AbstractConnection#setup}
 * method encounters an Exception
 * <p>
 * This means, that the Socket's OutputStream used for the SendingService is faulty or not connected. Normally this should
 * not happen. It will however most likely happen, if you provide your own Socket within {@link com.github.thorbenkuck.netcom2.network.server.ServerStart#setServerSocketFactory(com.github.thorbenkuck.netcom2.interfaces.Factory)}!
 * Therefore, if you encounter this Exception, check you implementation of the ServerSocketFactory.
 * <p>
 * Note, this Exception may contain multiple Exceptions. After the first Exception is encountered, the {@link com.github.thorbenkuck.netcom2.network.shared.clients.AbstractConnection}
 * tries to close its internally saved Socket using {@link java.net.Socket#close()}. This may throw another IOException. Those Exceptions
 * are suppressed within the first encountered Exception.
 *
 * @version 1.0
 * @see com.github.thorbenkuck.netcom2.network.shared.clients.AbstractConnection#setup
 * @see com.github.thorbenkuck.netcom2.network.interfaces.SendingService#setup(java.io.OutputStream, java.util.concurrent.BlockingQueue)
 * @see com.github.thorbenkuck.netcom2.network.interfaces.ReceivingService#setup(com.github.thorbenkuck.netcom2.network.shared.clients.Connection, Session)
 * @since 1.0
 */
public class ClientCreationFailedException extends NetComRuntimeException {

	/**
	 * {@inheritDoc}
	 */
	public ClientCreationFailedException(final String message) {
		super(message);
	}

	/**
	 * {@inheritDoc}
	 */
	public ClientCreationFailedException(final Throwable throwable) {
		super(throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	public ClientCreationFailedException(final String message, final Throwable throwable) {
		super(message, throwable);
	}
}
