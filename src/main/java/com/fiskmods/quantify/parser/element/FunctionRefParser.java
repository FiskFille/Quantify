package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.tree.Expression;
import com.fiskmods.quantify.parser.tree.FunctionRef;

import java.util.List;
import java.util.Optional;

class FunctionRefParser {
    private static FunctionRef parseFunction(final QtfParser parser, final Expression selector) throws QtfParseException {
        parser.startTree();
        parser.next(TokenClass.OPEN_PARENTHESIS);

        if (parser.consume(TokenClass.CLOSE_PARENTHESIS)) {
            return parser.newFunctionRef(selector, List.of());
        }

        final List<Expression> args = parser.nextSequence(ExpressionParser.INSTANCE, TokenClass.COMMA);
        parser.next(TokenClass.CLOSE_PARENTHESIS);

        return parser.newFunctionRef(selector, args);
    }

    static Optional<FunctionRef> tryParseFunction(final QtfParser parser, final Expression selector) throws QtfParseException {
        if (!parser.isNext(TokenClass.OPEN_PARENTHESIS)) {
            return Optional.empty();
        }
        return Optional.of(parseFunction(parser, selector));
    }
}
