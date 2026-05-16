package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.Expression;
import com.fiskmods.quantify.parser.tree.FunctionRef;

import java.util.List;

record FunctionRefParser(FunctionAddress func, boolean hasResult) implements SyntaxParser<FunctionRef> {
    static SyntaxParser<FunctionRef> parser(final FunctionAddress func, final boolean hasResult) {
        return new FunctionRefParser(func, hasResult);
    }

    public static SyntaxParser<Expression> tryParse(final String name, final Token.Range range, final Namespace namespace, final boolean hasResult) {
        return (parser, context) -> {
            if (!parser.isNext(TokenClass.OPEN_PARENTHESIS)) {
                return null;
            }

            final FunctionAddress func;
            try {
                func = namespace.getFunction(name);
            } catch (final QtfException e) {
                throw new QtfParseException(e, range);
            }
            return parser(func, hasResult).accept(parser, context);
        };
    }

    @Override
    public FunctionRef accept(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        parser.startTree();
        parser.next(TokenClass.OPEN_PARENTHESIS);

        if (parser.isNext(TokenClass.CLOSE_PARENTHESIS)) {
            func.validateParameters(0, parser.next().range());
            return parser.newFunctionRef(func, List.of());
        }

        final List<Expression> args = parser.nextSequence(ExpressionParser.INSTANCE, TokenClass.COMMA);
        func.validateParameters(args.size(), parser.next(TokenClass.CLOSE_PARENTHESIS).range());

        final FunctionRef ref = parser.newFunctionRef(func, args);
        if (!hasResult) {
            parser.expectLineBreak();
        }
        return ref;
    }
}
