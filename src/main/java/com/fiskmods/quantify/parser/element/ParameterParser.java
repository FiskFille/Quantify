package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.Identifier;
import com.fiskmods.quantify.parser.tree.ParameterTree;

class ParameterParser implements SyntaxParser<ParameterTree> {
    static final ParameterParser INSTANCE = new ParameterParser();

    @Override
    public ParameterTree accept(final QtfParser parser) throws QtfParseException {
        parser.startTree();
        final String name = parser.next(TokenClass.IDENTIFIER).getString();
        final Identifier type = TypeParser.parseType(parser);

        return parser.newParameter(name, type);
    }
}
