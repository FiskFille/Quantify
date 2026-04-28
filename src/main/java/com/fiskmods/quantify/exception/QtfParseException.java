package com.fiskmods.quantify.exception;

import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.parser.QtfParser;
import org.jspecify.annotations.Nullable;

public class QtfParseException extends QtfException {
    private final Token.@Nullable Range location;

    public QtfParseException(final String message, final Token.@Nullable Range location) {
        super(message);
        this.location = location;
    }

    public QtfParseException(final String message, final String reason, final Token.@Nullable Range location) {
        this(message + " - " + reason, location);
    }

    public QtfParseException(final String message) {
        this(message, null);
    }

    public QtfParseException(final Throwable cause) {
        this(cause.getMessage());
    }

    public int getStartIndex(final QtfParser parser) {
        if (location != null) {
            return location.startIndex();
        }
        final Token lastToken = parser.last();
        if (lastToken != null) {
            return lastToken.range().startIndex();
        }
        return 0;
    }

    public static QtfParseException internal(final String reason, final Token.Range location) {
        return new QtfParseException("Internal error", reason, location);
    }

    public static QtfParseException error(final String reason, final Token.Range location) {
        return new QtfParseException("Unresolved error", reason, location);
    }
}
