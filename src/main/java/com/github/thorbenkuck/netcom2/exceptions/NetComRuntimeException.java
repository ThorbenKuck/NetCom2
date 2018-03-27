package com.github.thorbenkuck.netcom2.exceptions;

/**
 * Any Exception, that is not declared at any interface within NetCom2 is a sub-class of this Exception.
 * <p>
 * You may catch this type, to catch any RuntimeException, coming from NetCom2, except for:
 * <p>
 * <ul>
 * <li>IllegalArgumentException</li>
 * <li>IllegalStateException</li>
 * <li>NullPointerException</li>
 * </ul>
 * <p>
 * There are no custom Exception-types for those Exceptions.
 *
 * @version 1.0
 * @since 1.0
 */
public abstract class NetComRuntimeException extends RuntimeException {

	/**
	 * {@inheritDoc}
	 */
	NetComRuntimeException(final String message) {
		super(message);
	}

	/**
	 * {@inheritDoc}
	 */
	NetComRuntimeException(final Throwable throwable) {
		super(throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	NetComRuntimeException(final String message, final Throwable throwable) {
		super(message, throwable);
	}

}
