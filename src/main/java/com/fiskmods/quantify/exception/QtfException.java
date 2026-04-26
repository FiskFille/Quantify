package com.fiskmods.quantify.exception;

public class QtfException extends Exception {
    public QtfException(final String message) {
        super(message);
    }

    public QtfException(final Throwable cause) {
        super(cause);
    }

    public QtfException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
