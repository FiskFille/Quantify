package com.fiskmods.quantify.lexer.token;

import com.fiskmods.quantify.exception.QtfParseException;

import java.util.ArrayList;
import java.util.List;

public final class TokenList {
    private TokenList() {}

    public static List<Token> parseNonEmpty(final TokenStream stream, final TokenClass tokenClass, final TokenClass delimiter) throws QtfParseException {
        final Token first = stream.next(tokenClass);
        if (!stream.isNext(delimiter)) {
            return List.of(first);
        }

        final List<Token> output = new ArrayList<>();
        output.add(first);

        do {
            stream.clearPeekedToken();
            final Token token = stream.next(tokenClass);
            output.add(token);
        } while (stream.isNext(delimiter));

        return output;
    }
}
