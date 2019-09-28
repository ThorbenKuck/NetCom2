package com.github.thorbenkuck.netcom2.exceptions;

public class PipelineAccessException extends NetComRuntimeException {

    public PipelineAccessException() {
    }

    public PipelineAccessException(String s) {
        super(s);
    }

    public PipelineAccessException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public PipelineAccessException(Throwable throwable) {
        super(throwable);
    }
}
