package com.github.thorbenkuck.netcom2.exceptions;

import com.github.thorbenkuck.netcom2.interfaces.Factory;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;

import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 * This Exception is thrown, if the {@link com.github.thorbenkuck.netcom2.network.shared.clients.AbstractConnection#setup}
 * method encounters an Exception
 * <p>
 * This means, that the Sockets OutputStream used for the SendingService is faulty or not connected. Normally this should
 * not happen. It will however most likely happen, if you provide your own Socket within {@link com.github.thorbenkuck.netcom2.network.server.ServerStart#setServerSocketFactory(Factory)}!
 * Therefor, if you encounter this Exception, check you implementation of the ServerSocketFactory.
 * <p>
 * Note, this Exception may contain multiple Exceptions. After the first Exception is encountered, the {@link com.github.thorbenkuck.netcom2.network.shared.clients.AbstractConnection}
 * tries to close its internally saved Socket using {@link Socket#close()}. This may throw another IOException. Those Exceptions
 * are suppressed within the first encountered Exception.
 *
 * @see com.github.thorbenkuck.netcom2.network.shared.clients.AbstractConnection#setup
 * @see com.github.thorbenkuck.netcom2.network.interfaces.SendingService#setup(OutputStream, BlockingQueue)
 * @see com.github.thorbenkuck.netcom2.network.interfaces.ReceivingService#setup(Connection, Session)
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
