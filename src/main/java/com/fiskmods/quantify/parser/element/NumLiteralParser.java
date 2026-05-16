package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.NumLiteral;

class NumLiteralParser implements SyntaxParser<NumLiteral> {
    public static final SyntaxParser<NumLiteral> PARSER = new NumLiteralParser();

    @Override
    public NumLiteral accept(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        parser.startTree();
        double value = parser.next(TokenClass.NUM_LITERAL).getNumber().doubleValue();
        if (parser.isNext(TokenClass.DEGREES)) {
            parser.clearPeekedToken();
            value *= Math.PI / 180;
        }
        return parser.newNumLiteral(value);
    }
}
