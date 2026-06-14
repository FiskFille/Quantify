package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.Expression;
import com.fiskmods.quantify.parser.tree.IfElseExpression;

class IfElseParser implements SyntaxParser<IfElseExpression> {
    static final IfElseParser INSTANCE = new IfElseParser();

    @Override
    public IfElseExpression accept(final QtfParser parser) throws QtfParseException {
        parser.startTree();
        parser.next(TokenClass.IF);
        final Expression condition = ExpressionParser.INSTANCE.accept(parser);
        parser.skip(TokenClass.TERMINATOR);

        final SyntaxParser<Expression> syntax = parser.consume(TokenClass.THEN)
                ? ExpressionParser.INSTANCE
                : ExpressionParser::parseBraceExpression;

        final Expression thenExpression = syntax.accept(parser);
        parser.skip(TokenClass.TERMINATOR);
        parser.next(TokenClass.ELSE);
        final Expression elseExpression = syntax.accept(parser);

        return parser.newIfElse(condition, thenExpression, elseExpression);
    }
}
