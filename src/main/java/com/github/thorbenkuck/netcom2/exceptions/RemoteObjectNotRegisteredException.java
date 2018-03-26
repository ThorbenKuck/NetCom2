package com.github.thorbenkuck.netcom2.exceptions;

/**
 * This Exception will be thrown, if no Object is registered for the requested Class inside the RMI API.
 *
 * @version 1.0
 * @see com.github.thorbenkuck.netcom2.network.client.RemoteObjectFactory
 * @see com.github.thorbenkuck.netcom2.interfaces.RemoteObjectRegistration
 * @since 1.0
 */
public class RemoteObjectNotRegisteredException extends RemoteRequestException {

	/**
	 * {@inheritDoc}
	 */
	public RemoteObjectNotRegisteredException(String message) {
		super(message);
	}

	/**
	 * {@inheritDoc}
	 */
	public RemoteObjectNotRegisteredException(Throwable throwable) {
		super(throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	public RemoteObjectNotRegisteredException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
