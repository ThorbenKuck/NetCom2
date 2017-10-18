package com.github.thorbenkuck.netcom2.exceptions;

public class PipelineAccessException extends NetComRuntimeException {
	public PipelineAccessException(String message) {
		super(message);
	}

	public PipelineAccessException(Throwable throwable) {
		super(throwable);
	}

	public PipelineAccessException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
