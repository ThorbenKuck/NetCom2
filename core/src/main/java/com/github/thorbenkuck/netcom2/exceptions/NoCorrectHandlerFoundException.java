package com.github.thorbenkuck.netcom2.exceptions;

/**
 * This RuntimeException will be thrown, if you call {@link com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline#to(Object)},
 * but no Method, annotated with {@link com.github.thorbenkuck.netcom2.annotations.ReceiveHandler} was found, which returned true on
 * {@link com.github.thorbenkuck.netcom2.annotations.ReceiveHandler#active()}
 *
 * @version 1.0
 * @since 1.0
 */
public class NoCorrectHandlerFoundException extends NetComRuntimeException {

	/**
	 * {@inheritDoc}
	 */
	public NoCorrectHandlerFoundException(final String message) {
		super(message);
	}

	/**
	 * {@inheritDoc}
	 */
	public NoCorrectHandlerFoundException(final Throwable throwable) {
		super(throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	public NoCorrectHandlerFoundException(final String message, final Throwable throwable) {
		super(message, throwable);
	}
}
