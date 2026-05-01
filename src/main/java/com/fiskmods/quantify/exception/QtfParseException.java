package com.fiskmods.quantify.exception;

import com.fiskmods.quantify.lexer.token.Token;

public class QtfParseException extends Exception {
    private final Token.Range range;

    public QtfParseException(final String message, final Token.Range range) {
        super(message);
        this.range = range;
    }

    public QtfParseException(final String message, final String reason, final Token.Range range) {
        this(message + " - " + reason, range);
    }

    public QtfParseException(final Throwable cause, final Token.Range range) {
        this(cause.getMessage(), range);
    }

    public Token.Range getRange() {
        return range;
    }

    public static QtfParseException internal(final String reason, final Token.Range range) {
        return new QtfParseException("Internal error", reason, range);
    }

    public static QtfParseException error(final String reason, final Token.Range range) {
        return new QtfParseException("Unresolved error", reason, range);
    }
}
