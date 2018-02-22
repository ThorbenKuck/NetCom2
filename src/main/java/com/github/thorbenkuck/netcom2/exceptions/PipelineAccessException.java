package com.github.thorbenkuck.netcom2.exceptions;

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
