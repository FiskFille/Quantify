package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.tree.BlockStatement;

class BlockParser {
    static BlockStatement parseBlock(final QtfParser parser) throws QtfParseException {
        parser.startTree();
        parser.next(TokenClass.OPEN_BRACES);
        final var statements = parser.parse(true);
        parser.next(TokenClass.CLOSE_BRACES);
        return parser.newBlockStatement(statements);
    }
}
