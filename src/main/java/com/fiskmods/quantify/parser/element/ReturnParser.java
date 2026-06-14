package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.Expression;
import com.fiskmods.quantify.parser.tree.ReturnStatement;

record ReturnParser() implements SyntaxParser<ReturnStatement> {
    static final ReturnParser INSTANCE = new ReturnParser();

    @Override
    public ReturnStatement accept(final QtfParser parser) throws QtfParseException {
        parser.startTree();
        parser.clearPeekedToken();
        final Expression e = ExpressionParser.INSTANCE.accept(parser);
        parser.skip(TokenClass.TERMINATOR);

        // Intentionally trigger exception if there are more tokens after return value
        if (!parser.isNext(TokenClass.CLOSE_BRACES)) {
            parser.next(TokenClass.CLOSE_BRACES);
        }
        return parser.newReturnStatement(e);
    }
}
