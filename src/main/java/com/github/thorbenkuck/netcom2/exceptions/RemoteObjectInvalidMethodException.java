package com.github.thorbenkuck.netcom2.exceptions;

/**
 * This Exception will be thrown, if the {@link com.github.thorbenkuck.netcom2.interfaces.RemoteObjectRegistration} receives
 * an {@link com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationRequest} with an declared method,
 * that the defined class does not have.
 *
 * @version 1.0
 * @see com.github.thorbenkuck.netcom2.interfaces.RemoteObjectRegistration
 * @since 1.0
 */
public class RemoteObjectInvalidMethodException extends RemoteRequestException {

	/**
	 * {@inheritDoc}
	 */
	public RemoteObjectInvalidMethodException(String message) {
		super(message);
	}

	/**
	 * {@inheritDoc}
	 */
	public RemoteObjectInvalidMethodException(Throwable throwable) {
		super(throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	public RemoteObjectInvalidMethodException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
