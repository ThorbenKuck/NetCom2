package com.github.thorbenkuck.netcom2.exceptions;

/**
 * This Exception will be thrown, if you try to call {@link com.github.thorbenkuck.netcom2.network.client.Sender#unRegistrationToServer(Class)}
 * on a class you have never registered.
 *
 * @version 1.0
 * @see com.github.thorbenkuck.netcom2.network.client.Sender#unRegistrationToServer(Class)
 * @see com.github.thorbenkuck.netcom2.network.client.Sender#unRegistrationToServer(Class, Class)
 * @see com.github.thorbenkuck.netcom2.network.client.Sender#unRegistrationToServer(Class, com.github.thorbenkuck.netcom2.network.shared.clients.Connection)
 * @since 1.0
 */
public class UnRegistrationException extends NetComRuntimeException {

	/**
	 * {@inheritDoc}
	 */
	public UnRegistrationException(final String message) {
		super(message);
	}

	/**
	 * {@inheritDoc}
	 */
	public UnRegistrationException(final Throwable throwable) {
		super(throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	public UnRegistrationException(final String message, final Throwable throwable) {
		super(message, throwable);
	}
}
