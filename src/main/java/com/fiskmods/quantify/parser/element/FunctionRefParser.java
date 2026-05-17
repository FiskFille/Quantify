package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.tree.Expression;
import com.fiskmods.quantify.parser.tree.FunctionRef;

import java.util.List;
import java.util.Optional;

class FunctionRefParser {
    private static FunctionRef parseFunction(final QtfParser parser, final FunctionAddress func) throws QtfParseException {
        parser.startTree();
        parser.next(TokenClass.OPEN_PARENTHESIS);

        if (parser.isNext(TokenClass.CLOSE_PARENTHESIS)) {
            func.validateParameters(0, parser.next().range());
            return parser.newFunctionRef(func, List.of());
        }

        final List<Expression> args = parser.nextSequence(ExpressionParser.INSTANCE, TokenClass.COMMA);
        func.validateParameters(args.size(), parser.next(TokenClass.CLOSE_PARENTHESIS).range());

        return parser.newFunctionRef(func, args);
    }

    static Optional<FunctionRef> parseFunction(final QtfParser parser, final String name, final Token.Range range, final Namespace namespace) throws QtfParseException {
        if (!parser.isNext(TokenClass.OPEN_PARENTHESIS)) {
            return Optional.empty();
        }

        final FunctionAddress func;
        try {
            func = namespace.get(name, MemberType.FUNCTION);
        } catch (final QtfException e) {
            throw new QtfParseException(e, range);
        }

        return Optional.of(parseFunction(parser, func));
    }
}
