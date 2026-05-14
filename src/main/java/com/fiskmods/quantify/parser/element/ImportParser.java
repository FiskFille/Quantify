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

class ImportParser implements SyntaxParser<Object> {
    static final ImportParser INSTANCE = new ImportParser();

    @Override
    public Object accept(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        parser.clearPeekedToken();

        final Token token = parser.next(TokenClass.STR_LITERAL);
        final String key = token.getString();
        parser.next(TokenClass.COLON);

        final String name = parser.next(TokenClass.IDENTIFIER).getString();
        try {
            final QtfLibrary library = context.libraries().getLibrary(key);
            context.addMember(name, MemberType.LIBRARY, library);
        } catch (final QtfException e) {
            throw new QtfParseException(e, token.range());
        }

        parser.expectLineBreak();
        return null;
    }
}
