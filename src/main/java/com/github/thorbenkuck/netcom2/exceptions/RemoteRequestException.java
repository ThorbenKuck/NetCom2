package com.github.thorbenkuck.netcom2.exceptions;

public class RemoteRequestException extends RuntimeException {

    public RemoteRequestException(String message) {
        super(message);
    }

    public RemoteRequestException(Throwable throwable) {
        super(throwable);
    }

    public RemoteRequestException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
