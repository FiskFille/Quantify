package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.VarInfo;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.MemberMap;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

record VariableParser(boolean isPublic) implements SyntaxParser<JvmFunction> {
    static final VariableParser LOCAL = new VariableParser(false);
    static final VariableParser PUBLIC = new VariableParser(true);

    @Override
    @SuppressWarnings("unchecked")
    public JvmFunction accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
        if (isPublic) {
            parser.clearPeekedToken(); // PUBLIC
            parser.next(TokenClass.VAR);
        } else {
            parser.clearPeekedToken(); // VAR
        }

        MemberMap members = context.scope().members;
        String name = nextName(parser, members);

        if (!parser.isNext(TokenClass.COMMA)) {
            VarType<?> type = extractType(parser).orElse(VarType.NUM);
            Assignable var = VarInfo.define(name, type, context, isPublic);
            return assignOrInit(parser, var, type, isPublic);
        }

        List<String> list = new ArrayList<>();
        list.add(name);
        do {
            parser.clearPeekedToken();
            list.add(nextName(parser, members));
        } while (parser.isNext(TokenClass.COMMA));

        VarType<?> type = extractType(parser).orElse(VarType.NUM);
        List<VarAddress<?>> vars = new ArrayList<>();

        for (String varName : list) {
            vars.add(VarInfo.define(varName, type, context, isPublic));
        }
        Assignable var = new VariableList<>(vars.toArray(new VarAddress[0]));
        return assignOrInit(parser, var, type, isPublic);
    }

    private static JvmFunction assignOrInit(
            QtfParser parser, Assignable assignable, VarType<?> type, boolean isPublic) throws QtfParseException {
        if (type.isAssignable() && parser.isNext(TokenClass.ASSIGNMENT, null)) {
            return parser.next(Assignment.parser(assignable, true));
        }

        // Public var storage needs no initialization
        if (isPublic) {
            return null;
        }
        return assignable::init;
    }

    private static String nextName(QtfParser parser, MemberMap members) throws QtfParseException {
        String name = parser.next(TokenClass.IDENTIFIER).getString();
        try {
            members.nameCheck(name);
        } catch (QtfException e) {
            throw new QtfParseException(e);
        }
        return name;
    }

    private static Optional<VarType<?>> extractType(QtfParser parser) throws QtfParseException {
        if (!parser.isNext(TokenClass.COLON)) {
            return Optional.empty();
        }
        parser.clearPeekedToken();
        String name = parser.next(TokenClass.IDENTIFIER).getString();
        try {
            return Optional.of(VarType.getType(name));
        } catch (QtfException e) {
            throw new QtfParseException(e);
        }
    }

    static <T extends Value & Assignable> SyntaxParser<VarAddress<T>> refOrDef(VarType<T> type, int modifiers) {
        return (modifiers & VarInfo.DEFINITION) != 0 ? def(type, modifiers) : ref(type);
    }

    static <T extends Value & Assignable> SyntaxParser<VarAddress<T>> ref(VarType<T> type) {
        return IdentifierParser.from((name, namespace) ->
                (parser, context) -> compute(name, namespace, type, 0));
    }

    static <T extends Value & Assignable> SyntaxParser<VarAddress<T>> def(VarType<T> type, int modifiers) {
        return (parser, context) -> {
            String name = parser.next(TokenClass.IDENTIFIER).getString();
            return parser.next(def(name, type, modifiers));
        };
    }

    static <T extends Value & Assignable> SyntaxParser<VarAddress<T>> def(String name, VarType<T> type, int modifiers) {
        // Definitions always belong to the default namespace
        return (parser, context) ->
                compute(name, context.getDefaultNamespace(), type, modifiers);
    }

    static <T extends Value & Assignable> VarAddress<T> compute(
            String name, Namespace namespace, VarType<T> type, int modifiers)
            throws QtfParseException {
        try {
            return namespace.computeVariable(type, name, modifiers);
        } catch (QtfException e) {
            throw new QtfParseException(e);
        }
    }
}
