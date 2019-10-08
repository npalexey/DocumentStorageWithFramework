package com.nikitiuk.documentstoragewithframework.exceptions;

public class AlreadyExistsException extends Exception {

    public AlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlreadyExistsException(String message) {
        super(message);
    }

    public AlreadyExistsException(Throwable cause) {
        super(cause);
    }
}
