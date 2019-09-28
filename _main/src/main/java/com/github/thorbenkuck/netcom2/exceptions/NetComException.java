package com.github.thorbenkuck.netcom2.exceptions;

public class NetComException extends Exception {

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

}
