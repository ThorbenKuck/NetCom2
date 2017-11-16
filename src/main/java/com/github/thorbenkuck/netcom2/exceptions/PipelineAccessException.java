package com.github.thorbenkuck.netcom2.exceptions;

public class PipelineAccessException extends NetComRuntimeException {
	public PipelineAccessException(final String message) {
		super(message);
	}

	public PipelineAccessException(final Throwable throwable) {
		super(throwable);
	}

	public PipelineAccessException(final String message, final Throwable throwable) {
		super(message, throwable);
	}
}
