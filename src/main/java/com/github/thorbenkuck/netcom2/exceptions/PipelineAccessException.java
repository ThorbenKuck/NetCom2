package com.github.thorbenkuck.netcom2.exceptions;

/**
 * This Exception will be thrown if you illegally Access an {@link com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline}.
 *
 * @version 1.0
 * @see com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline
 * @since 1.0
 */
public class PipelineAccessException extends NetComRuntimeException {

	/**
	 * {@inheritDoc}
	 */
	public PipelineAccessException(final String message) {
		super(message);
	}

	/**
	 * {@inheritDoc}
	 */
	public PipelineAccessException(final Throwable throwable) {
		super(throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	public PipelineAccessException(final String message, final Throwable throwable) {
		super(message, throwable);
	}
}
