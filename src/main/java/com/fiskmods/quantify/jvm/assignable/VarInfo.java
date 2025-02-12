package com.fiskmods.quantify.jvm.assignable;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.element.Assignable;
import com.fiskmods.quantify.parser.element.Value;

public record VarInfo<T extends Value & Assignable>(String name, VarType<T> type) {
    public static final int DEFINITION = 0x1;
    public static final int PUBLIC = 0x2;

    public VarAddress<T> define(SyntaxContext context, boolean isPublic) throws QtfParseException {
        if (isPublic) {
            return context.addPublicVar(name, type);
        }
        try {
            return type.defineLocal(name, context.scope());
        } catch (QtfException e) {
            throw new QtfParseException(e);
        }
    }

    public static <T extends Value & Assignable> VarAddress<T> define(
            String name, VarType<T> type, SyntaxContext context, boolean isPublic) throws QtfParseException {
        if (isPublic) {
            return context.addPublicVar(name, type);
        }
        try {
            return type.defineLocal(name, context.scope());
        } catch (QtfException e) {
            throw new QtfParseException(e);
        }
    }

    public static <T extends Value & Assignable> VarAddress<T> define(
            String name, VarType<T> type, SyntaxContext context, int modifiers) throws QtfParseException {
        return define(name, type, context, (modifiers & PUBLIC) != 0);
    }

    public static VarInfo<?> parse(QtfParser parser) throws QtfParseException {
        String name = parser.next(TokenClass.IDENTIFIER).getString();
        VarType<?> type = VarType.NUM;

        // Explicit type definition
        if (parser.isNext(TokenClass.COLON)) {
            parser.clearPeekedToken();
            String typeName = parser.next(TokenClass.IDENTIFIER).getString();
            try {
                type = VarType.getType(typeName);
            } catch (QtfException e) {
                throw new QtfParseException(e);
            }
        }
        return new VarInfo<>(name, type);
    }
}
