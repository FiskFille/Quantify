package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.Return;
import com.fiskmods.quantify.parser.tree.Value;

record ReturnParser() implements SyntaxParser<Return> {
    static final ReturnParser INSTANCE = new ReturnParser();

    @Override
    public Return accept(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        parser.clearPeekedToken();
        final Value value = ExpressionParser.INSTANCE.accept(parser, context);
        parser.skip(TokenClass.TERMINATOR);

        // Intentionally trigger exception if there are more tokens after return value
        if (!parser.isNext(TokenClass.CLOSE_BRACES)) {
            parser.next(TokenClass.CLOSE_BRACES);
        }
        return new Return(value);
    }
}
