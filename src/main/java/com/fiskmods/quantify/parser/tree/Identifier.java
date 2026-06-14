package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.Token;

public final class Identifier extends AbstractMemberExpression {
    private final String name;

    private Identifier(final String name, final Token.Range range) {
        this.name = name;
        this.range = range;
    }

    public static Identifier from(final Token token) throws QtfParseException {
        return new Identifier(token.getString(), token.range());
    }

    public String name() {
        return name;
    }
}
