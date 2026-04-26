package com.fiskmods.quantify.exception;

import com.fiskmods.quantify.lexer.TextScanner;
import org.jspecify.annotations.Nullable;

public class QtfLexerException extends QtfException {
    private final TextScanner.@Nullable Location location;

    public QtfLexerException(final String message, final TextScanner.@Nullable Location location) {
        super(message);
        this.location = location;
    }

    public QtfLexerException(final String message) {
        this(message, null);
    }

    public TextScanner.@Nullable Location location() {
        return location;
    }
}
