package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.ImportStatement;

class ImportParser implements SyntaxParser<ImportStatement> {
    static final ImportParser INSTANCE = new ImportParser();

    @Override
    public ImportStatement accept(final QtfParser parser) throws QtfParseException {
        parser.startTree();
        parser.next(TokenClass.IMPORT);
        final String key = parser.next(TokenClass.STR_LITERAL).getString();
        parser.next(TokenClass.COLON);
        final String name = parser.next(TokenClass.IDENTIFIER).getString();

        final ImportStatement statement = parser.newImportStatement(name, key);
        parser.expectLineBreak();
        return statement;
    }
}
