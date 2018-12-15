package com.github.thorbenkuck.netcom2.exceptions;

/**
 * This RuntimeException, will be thrown, if any Object-send fails for any reason.
 *
 * @version 1.0
 * @see com.github.thorbenkuck.netcom2.network.shared.connections.Connection#write(byte[])
 * @see com.github.thorbenkuck.netcom2.network.shared.connections.Connection#write(String)
 * @see com.github.thorbenkuck.netcom2.network.shared.Session#send(Object)
 * @see com.github.thorbenkuck.netcom2.network.shared.clients.Client#send(Object)
 * @see com.github.thorbenkuck.netcom2.network.shared.clients.Client#send(Class, Object)
 * @see com.github.thorbenkuck.netcom2.network.shared.clients.Client#send(Object, com.github.thorbenkuck.netcom2.network.shared.connections.Connection)
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
