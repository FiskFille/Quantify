package com.fiskmods.quantify.parser;

import com.fiskmods.quantify.exception.QtfParseException;

@FunctionalInterface
public interface SyntaxParser<T> {
    T accept(QtfParser parser, SyntaxContext context) throws QtfParseException;

    default SyntaxParser<T> or(final SyntaxParser<? extends T> other) {
        return (parser, context) -> {
            final T result = accept(parser, context);
            return result != null ? result : other.accept(parser, context);
        };
    }
}
