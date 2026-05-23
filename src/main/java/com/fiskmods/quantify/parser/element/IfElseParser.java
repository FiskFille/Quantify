package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.Expression;
import com.fiskmods.quantify.parser.tree.IfElseExpression;

class IfElseParser implements SyntaxParser<IfElseExpression> {
    static final IfElseParser INSTANCE = new IfElseParser();

    @Override
    public IfElseExpression accept(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        parser.startTree();
        parser.next(TokenClass.IF);
        final Expression condition = ExpressionParser.INSTANCE.accept(parser, context);
        parser.skip(TokenClass.TERMINATOR);

        final SyntaxParser<Expression> syntax;
        if (parser.isNext(TokenClass.THEN)) {
            parser.clearPeekedToken();
            syntax = ExpressionParser.INSTANCE;
        } else {
            syntax = ExpressionParser::parseBraceExpression;
        }

        final Expression thenExpression = syntax.accept(parser, context);
        parser.skip(TokenClass.TERMINATOR);
        parser.next(TokenClass.ELSE);
        final Expression elseExpression = syntax.accept(parser, context);

        return parser.newIfElse(condition, thenExpression, elseExpression);
    }
}
