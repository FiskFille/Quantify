package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.VarInfo;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.lexer.token.TokenList;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.Assignable;
import com.fiskmods.quantify.parser.tree.Value;
import com.fiskmods.quantify.parser.tree.VarDefinitionTree;
import com.fiskmods.quantify.parser.tree.VariableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

record VariableParser(boolean isPublic) implements SyntaxParser<VarDefinitionTree> {
    static final VariableParser LOCAL = new VariableParser(false);
    static final VariableParser PUBLIC = new VariableParser(true);

    @Override
    public VarDefinitionTree accept(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        if (isPublic)
            parser.next(TokenClass.PUBLIC);
        parser.next(TokenClass.VAR);

        final List<Token> identifiers = TokenList.parseNonEmpty(parser, TokenClass.IDENTIFIER, TokenClass.COMMA);
        final VarType<?> type = extractType(parser).orElse(VarType.NUM);

        if (identifiers.size() == 1) {
            return parseSingleVar(parser, context, type, identifiers.getFirst());
        } else {
            return parseMultiVar(parser, context, type, identifiers);
        }
    }

    private VarDefinitionTree parseSingleVar(final QtfParser parser, final SyntaxContext context, final VarType<?> type, final Token identifier) throws QtfParseException {
        final String name = identifier.getString();
        final VarAddress var;

        try {
            var = VarInfo.define(name, type, context, isPublic);
        } catch (final QtfException e) {
            throw new QtfParseException(e, identifier.range());
        }
        return assignOrInit(parser, context, var, type);
    }

    private <T extends VarAddress> VarDefinitionTree parseMultiVar(final QtfParser parser, final SyntaxContext context, final VarType<T> type, final List<Token> identifiers) throws QtfParseException {
        final List<VarAddress> vars = new ArrayList<>(identifiers.size());

        for (final Token identifier : identifiers) {
            final String varName = identifier.getString();
            try {
                vars.add(VarInfo.define(varName, type, context, isPublic));
            } catch (final QtfException e) {
                throw new QtfParseException(e, identifier.range());
            }
        }

        final Assignable var = new VariableList(vars);
        return assignOrInit(parser, context, var, type);
    }

    private VarDefinitionTree assignOrInit(final QtfParser parser, final SyntaxContext context, final Assignable assignable, final VarType<?> type) throws QtfParseException {
        final Value initializer;

        if (type.isAssignable() && parser.isNext(TokenClass.ASSIGNMENT, null)) {
            parser.next(TokenClass.ASSIGNMENT);
            initializer = ExpressionParser.INSTANCE.accept(parser, context);
        } else {
            initializer = null;
        }
        return new VarDefinitionTree(assignable, type, initializer, isPublic);
    }

    private static Optional<VarType<?>> extractType(final QtfParser parser) throws QtfParseException {
        if (!parser.isNext(TokenClass.COLON)) {
            return Optional.empty();
        }

        parser.clearPeekedToken();
        final Token identifier = parser.next(TokenClass.IDENTIFIER);
        final String name = identifier.getString();

        try {
            return Optional.of(VarType.getType(name));
        } catch (final QtfException e) {
            throw new QtfParseException(e, identifier.range());
        }
    }

    static <T extends VarAddress> SyntaxParser<T> refOrDef(final VarType<T> type, final int modifiers) {
        return (modifiers & VarInfo.DEFINITION) != 0 ? def(type, modifiers) : ref(type);
    }

    static <T extends VarAddress> SyntaxParser<T> ref(final VarType<T> type) {
        return IdentifierParser.from((name, range, namespace)
                -> (parser, context) -> compute(name, range, namespace, type, 0)
        );
    }

    static <T extends VarAddress> SyntaxParser<T> def(final VarType<T> type, final int modifiers) {
        return (parser, context) -> {
            final Token identifier = parser.next(TokenClass.IDENTIFIER);
            final String name = identifier.getString();

            return def(name, identifier.range(), type, modifiers).accept(parser, context);
        };
    }

    static <T extends VarAddress> SyntaxParser<T> def(final String name, final Token.Range range, final VarType<T> type, final int modifiers) {
        // Definitions always belong to the default namespace
        return (parser, context) -> compute(name, range, context.getDefaultNamespace(), type, modifiers);
    }

    static <T extends VarAddress> T compute(final String name, final Token.Range range, final Namespace namespace, final VarType<T> type, final int modifiers) throws QtfParseException {
        try {
            return namespace.computeVariable(type, name, modifiers);
        } catch (final QtfException e) {
            throw new QtfParseException(e, range);
        }
    }

    static SyntaxParser<VariableList> parseList(final VarAddress firstVar, final int modifiers) {
        return (parser, context) -> {
            final List<VarAddress> list = new ArrayList<>();
            list.add(firstVar);
            do {
                parser.clearPeekedToken();
                list.add(AssignableParser.nextVariable(parser, context, firstVar.type(), modifiers));
            } while (parser.isNext(TokenClass.COMMA));

            return new VariableList(list);
        };
    }
}
