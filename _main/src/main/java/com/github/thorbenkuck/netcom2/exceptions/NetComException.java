package com.github.thorbenkuck.netcom2.exceptions;

public class NetComException extends RuntimeException {

    public NetComException() {
    }

    public NetComException(String s) {
        super(s);
    }

    public NetComException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public NetComException(Throwable throwable) {
        super(throwable);
    }

    public NetComException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
