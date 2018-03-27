package com.github.thorbenkuck.netcom2.exceptions;

/**
 * This Exception is the super-Exception for any non-runtime Exception within NetCom2.
 * <p>
 * So, if you want, you can simply catch this Exception, if multiple Exceptions are thrown anywhere.
 * <p>
 * You cant however create a custom <code>NetComException</code>! The Constructors of this Exception are package-private
 * so that only Exceptions defined within this package are NetComExceptions.
 *
 * @version 1.0
 * @since 1.0
 */
public abstract class NetComException extends Exception {

	/**
	 * {@inheritDoc}
	 */
	NetComException(final String message) {
		super(message);
	}

	/**
	 * {@inheritDoc}
	 */
	NetComException(final Throwable throwable) {
		super(throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	NetComException(final String message, final Throwable throwable) {
		super(message, throwable);
	}

}
