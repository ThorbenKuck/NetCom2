package com.github.thorbenkuck.netcom2.exceptions;

/**
 * This RuntimeException will be thrown, if any Connection entry or exit point fails to be setUp.
 *
 * @version 1.0
 * @see com.github.thorbenkuck.netcom2.network.interfaces.ReceivingService
 * @see com.github.thorbenkuck.netcom2.network.interfaces.SendingService
 * @since 1.0
 */
public class SetupListenerException extends NetComRuntimeException {

	/**
	 * {@inheritDoc}
	 */
	public SetupListenerException(final String message) {
		super(message);
	}

	/**
	 * {@inheritDoc}
	 */
	public SetupListenerException(final Throwable throwable) {
		super(throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	public SetupListenerException(final String message, final Throwable throwable) {
		super(message, throwable);
	}
}
