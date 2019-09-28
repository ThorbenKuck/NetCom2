package com.github.thorbenkuck.netcom2.exceptions;

public class NetComRuntimeException extends RuntimeException {

    public NetComRuntimeException() {
    }

    public NetComRuntimeException(String s) {
        super(s);
    }

    public NetComRuntimeException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public NetComRuntimeException(Throwable throwable) {
        super(throwable);
    }
}
