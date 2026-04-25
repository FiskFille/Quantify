package com.fiskmods.quantify.exception;

import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.parser.QtfParser;
import org.jspecify.annotations.Nullable;

public class QtfParseException extends QtfException {
    private final @Nullable String reason;
    private final Token.@Nullable Range location;

    public QtfParseException(String message, String reason, Token.@Nullable Range location) {
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

    public @Nullable String getReason() {
        return reason;
    }

    public Token.@Nullable Range getLocation(QtfParser parser) {
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
