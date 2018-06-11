package com.github.thorbenkuck.netcom2.exceptions;

import com.github.thorbenkuck.netcom2.network.shared.session.Session;

/**
 * This RuntimeException, will be thrown, if any Object-send fails for any reason.
 *
 * @version 1.0
 * @see com.github.thorbenkuck.netcom2.network.shared.clients.Connection#write(Object)
 * @see Session#send(Object)
 * @see com.github.thorbenkuck.netcom2.network.shared.clients.Client#send(Object)
 * @see com.github.thorbenkuck.netcom2.network.shared.clients.Client#send(Class, Object)
 * @see com.github.thorbenkuck.netcom2.network.shared.clients.Client#send(com.github.thorbenkuck.netcom2.network.shared.clients.Connection, Object)
 * @since 1.0
 */
public class SendFailedException extends NetComRuntimeException {

	/**
	 * {@inheritDoc}
	 */
	public SendFailedException(final String message) {
		super(message);
	}

	/**
	 * {@inheritDoc}
	 */
	public SendFailedException(final Throwable throwable) {
		super(throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	public SendFailedException(final String message, final Throwable throwable) {
		super(message, throwable);
	}
}
