package com.fiskmods.quantify.exception;

import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.parser.QtfParser;

import javax.annotation.Nullable;

public class QtfParseException extends QtfException {
    @Nullable
    private final String reason;
    @Nullable
    private final Token.Range location;

    public QtfParseException(String message, String reason, @Nullable Token.Range location) {
        super(message);
        this.reason = reason;
        this.location = location;
    }

    public QtfParseException(String message) {
        super(message);
        this.reason = null;
        this.location = null;
    }

    public QtfParseException(Throwable cause) {
        this(cause.getMessage());
    }

    @Nullable
    public String getReason() {
        return reason;
    }

    @Nullable
    public Token.Range getLocation(QtfParser parser) {
        if (location != null) {
            return location;
        }
        Token lastToken = parser.last();
        return lastToken != null ? lastToken.range() : null;
    }

    public static QtfParseException internal(String reason, Token.Range location) {
        return new QtfParseException("Internal error", reason, location);
    }

    public static QtfParseException error(String reason, Token.Range location) {
        return new QtfParseException("Unresolved error", reason, location);
    }
}
