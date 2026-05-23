package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.library.QtfLibrary;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.Identifier;
import com.fiskmods.quantify.parser.tree.ImportStatement;

class ImportParser implements SyntaxParser<ImportStatement> {
    static final ImportParser INSTANCE = new ImportParser();

    @Override
    public ImportStatement accept(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        parser.startTree();
        parser.clearPeekedToken();

        final Token token = parser.next(TokenClass.STR_LITERAL);
        final String key = token.getString();
        parser.next(TokenClass.COLON);

        final Identifier name = Identifier.from(parser.next(TokenClass.IDENTIFIER));
        final QtfLibrary library = addLibrary(context, name.name(), key, token.range());

        final ImportStatement statement = parser.newImportStatement(name, key, library);
        parser.expectLineBreak();
        return statement;
    }

    private QtfLibrary addLibrary(final SyntaxContext context, final String name, final String key, final Token.Range range) throws QtfParseException {
        try {
            final QtfLibrary library = context.libraries().getLibrary(key);
            context.addMember(name, MemberType.LIBRARY, library);
            return library;
        } catch (final QtfException e) {
            throw new QtfParseException(e, range);
        }
    }
}
