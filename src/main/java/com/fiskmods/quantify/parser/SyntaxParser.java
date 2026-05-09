package com.fiskmods.quantify.parser;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.parser.tree.Tree;

import java.util.function.Function;

@FunctionalInterface
public interface SyntaxParser<T extends Tree> {
    T accept(QtfParser parser, SyntaxContext context) throws QtfParseException;

    default SyntaxParser<?> or(final SyntaxParser<?> other) {
        return (parser, context) -> {
            final T result = accept(parser, context);
            return result != null ? result : other.accept(parser, context);
        };
    }

    default <R extends Tree> SyntaxParser<R> sequence(final Function<T, SyntaxParser<R>> func) {
        return (parser, context) -> func.apply(accept(parser, context)).accept(parser, context);
    }

    default <R extends Tree> SyntaxParser<R> map(final Function<T, R> func) {
        return (parser, context) -> func.apply(accept(parser, context));
    }
}
