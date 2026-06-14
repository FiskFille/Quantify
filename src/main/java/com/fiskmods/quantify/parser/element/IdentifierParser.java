package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.tree.*;

import java.util.Optional;

class IdentifierParser {
    static Expression parseIdentifier(final QtfParser parser) throws QtfParseException {
        Expression result = Identifier.from(parser.next(TokenClass.IDENTIFIER));

        while (parser.isNext(TokenClass.DOT)) {
            parser.startTree();
            parser.clearPeekedToken();
            final String name = parser.next(TokenClass.IDENTIFIER).getString();
            result = parser.newMemberSelect(result, name);
        }
        return result;
    }

    static Statement parseStatementIdentifier(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        final Expression expression = parseIdentifier(parser);
        final Optional<FunctionRef> func = FunctionRefParser.tryParseFunction(parser, expression);
        if (func.isPresent()) {
            parser.expectLineBreak();
            return ExpressionStatement.of(func.get());
        }

        final Statement statement = AssignmentParser.parseAssignment(parser, context, expression);
        parser.expectLineBreak();
        return statement;
    }

    static Expression parseExpressionIdentifier(final QtfParser parser) throws QtfParseException {
        final Expression expression = parseIdentifier(parser);
        final Optional<FunctionRef> func = FunctionRefParser.tryParseFunction(parser, expression);
        if (func.isPresent()) {
            return func.get();
        }
        return expression;
    }
}
