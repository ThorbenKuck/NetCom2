package com.github.thorbenkuck.netcom2.exceptions;

import com.github.thorbenkuck.netcom2.network.server.RemoteObjectRegistration;

/**
 * This Exception will be thrown if anything goes wrong within the {@link RemoteObjectRegistration}.
 *
 * @version 1.0
 * @see RemoteObjectRegistration
 * @since 1.0
 */
public class HandlerInvocationException extends NetComRuntimeException {

	/**
	 * {@inheritDoc}
	 */
	public HandlerInvocationException(final String message) {
		super(message);
	}

	/**
	 * {@inheritDoc}
	 */
	public HandlerInvocationException(final Throwable throwable) {
		super(throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	public HandlerInvocationException(final String message, final Throwable throwable) {
		super(message, throwable);
	}
}
