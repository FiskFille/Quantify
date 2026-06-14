package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.tree.Identifier;
import org.jspecify.annotations.Nullable;

class TypeParser {
    static @Nullable Identifier parseType(final QtfParser parser) throws QtfParseException {
        if (parser.consume(TokenClass.COLON)) {
            return Identifier.from(parser.next(TokenClass.IDENTIFIER));
        }
        return null;
    }
}
