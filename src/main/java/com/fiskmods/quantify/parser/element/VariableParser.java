package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.VarInfo;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.lexer.token.TokenList;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.Expression;
import com.fiskmods.quantify.parser.tree.VarDefinitionTree;
import com.fiskmods.quantify.parser.tree.VarRef;
import org.jspecify.annotations.Nullable;

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
        final Expression initializer = extractInitializer(parser, context, type);
        final List<VarRef> vars;

        if (identifiers.size() == 1) {
            vars = List.of(defineVar(context, type, identifiers.getFirst()));
        } else {
            vars = new ArrayList<>(identifiers.size());
            for (final Token identifier : identifiers) {
                vars.add(defineVar(context, type, identifier));
            }
        }
        return new VarDefinitionTree(vars, type, initializer, isPublic);
    }

    private VarRef defineVar(final SyntaxContext context, final VarType<?> type, final Token identifier) throws QtfParseException {
        final String name = identifier.getString();
        try {
            final VarAddress address = VarInfo.define(name, type, context, isPublic);
            return new VarRef(address, false);
        } catch (final QtfException e) {
            throw new QtfParseException(e, identifier.range());
        }
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

    private static @Nullable Expression extractInitializer(final QtfParser parser, final SyntaxContext context, final VarType<?> type) throws QtfParseException {
        if (type.isAssignable() && parser.isNext(TokenClass.ASSIGNMENT, null)) {
            parser.next(TokenClass.ASSIGNMENT);
            return ExpressionParser.INSTANCE.accept(parser, context);
        }
        return null;
    }

    static VarRef compute(final String name, final Token.Range range, final Namespace namespace, final VarType<?> type, final boolean isNegated) throws QtfParseException {
        try {
            final VarAddress address = namespace.computeVariable(type, name);
            return new VarRef(address, isNegated);
        } catch (final QtfException e) {
            throw new QtfParseException(e, range);
        }
    }

    static VarRef parseVariable(final QtfParser parser, final SyntaxContext context, final VarType<?> type) throws QtfParseException {
        final boolean isNegated;
        if (parser.isNext(TokenClass.OPERATOR, Operator.SUB)) {
            parser.clearPeekedToken();
            isNegated = true;
        } else {
            isNegated = false;
        }

        return IdentifierParser.parseIdentifier(parser, context,
                (name, range, namespace) -> compute(name, range, namespace, type, isNegated)
        );
    }

    static List<VarRef> parseList(final QtfParser parser, final SyntaxContext context, final VarRef firstVar) throws QtfParseException {
        final List<VarRef> list = new ArrayList<>();
        list.add(firstVar);
        do {
            parser.clearPeekedToken();
            list.add(parseVariable(parser, context, firstVar.address().type()));
        } while (parser.isNext(TokenClass.COMMA));

        return list;
    }
}
