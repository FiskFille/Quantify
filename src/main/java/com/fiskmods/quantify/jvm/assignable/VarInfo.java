package com.fiskmods.quantify.jvm.assignable;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;

public record VarInfo<T extends VarAddress>(String name, VarType<T> type) {
    public T define(final SyntaxContext context, final boolean isPublic) throws QtfException {
        if (isPublic) {
            return context.addPublicVar(name, type);
        } else {
            return type.defineLocal(name, context.scope());
        }
    }

    public static <T extends VarAddress> T define(final String name, final VarType<T> type, final SyntaxContext context, final boolean isPublic) throws QtfException {
        if (isPublic) {
            return context.addPublicVar(name, type);
        } else {
            return type.defineLocal(name, context.scope());
        }
    }

    public static VarInfo<?> parse(final QtfParser parser) throws QtfParseException {
        final String name = parser.next(TokenClass.IDENTIFIER).getString();
        VarType<?> type = VarType.NUM;

        // Explicit type definition
        if (parser.consume(TokenClass.COLON)) {
            final Token identifier = parser.next(TokenClass.IDENTIFIER);
            final String typeName = identifier.getString();
            try {
                type = VarType.getType(typeName);
            } catch (final QtfException e) {
                throw new QtfParseException(e, identifier.range());
            }
        }
        return new VarInfo<>(name, type);
    }
}
