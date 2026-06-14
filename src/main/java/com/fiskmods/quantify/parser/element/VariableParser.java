package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.lexer.token.TokenList;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.Expression;
import com.fiskmods.quantify.parser.tree.Identifier;
import com.fiskmods.quantify.parser.tree.VarDefinitionTree;
import com.fiskmods.quantify.parser.tree.VarRef;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record VariableParser(boolean isPublic) implements SyntaxParser<VarDefinitionTree> {
    static final VariableParser LOCAL = new VariableParser(false);
    static final VariableParser PUBLIC = new VariableParser(true);

    @Override
    public VarDefinitionTree accept(final QtfParser parser) throws QtfParseException {
        parser.startTree();

        if (isPublic)
            parser.next(TokenClass.PUBLIC);
        parser.next(TokenClass.VAR);

        final List<Token> identifiers = TokenList.parseNonEmpty(parser, TokenClass.IDENTIFIER, TokenClass.COMMA);
        final Identifier type = TypeParser.parseType(parser);
        final Expression initializer = extractInitializer(parser);

        final List<String> names = new ArrayList<>(identifiers.size());

        if (identifiers.size() == 1) {
            names.add(identifiers.getFirst().getString());
        } else {
            for (final Token token : identifiers) {
                names.add(token.getString());
            }
        }

        return parser.newVariable(names, type, initializer, isPublic);
    }

    private static @Nullable Expression extractInitializer(final QtfParser parser) throws QtfParseException {
        if (parser.isNext(TokenClass.ASSIGNMENT)) {
            parser.next(TokenClass.ASSIGNMENT);
            return ExpressionParser.INSTANCE.accept(parser);
        }
        return null;
    }

    static VarRef parseVariable(final QtfParser parser) throws QtfParseException {
        final boolean isNegated;
        if (parser.isNext(TokenClass.OPERATOR, Operator.SUB)) {
            parser.clearPeekedToken();
            isNegated = true;
        } else {
            isNegated = false;
        }

        final Expression expression = IdentifierParser.parseIdentifier(parser);
        return parser.newVariableRef(expression, isNegated);
    }

    static List<VarRef> parseList(final QtfParser parser, final VarRef firstVar) throws QtfParseException {
        final List<VarRef> list = new ArrayList<>();
        list.add(firstVar);
        do {
            parser.clearPeekedToken();
            list.add(parseVariable(parser));
        } while (parser.isNext(TokenClass.COMMA));

        return list;
    }
}
