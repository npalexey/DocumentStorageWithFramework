package com.nikitiuk.documentstoragewithframework.exceptions;

public class NoValidDataFromSourceException extends Exception {

    public NoValidDataFromSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoValidDataFromSourceException(String message) {
        super(message);
    }

    public NoValidDataFromSourceException(Throwable cause) {
        super(cause);
    }
}