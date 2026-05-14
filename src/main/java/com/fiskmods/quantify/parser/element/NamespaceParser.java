package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.Keywords;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.library.QtfLibrary;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;

class NamespaceParser implements SyntaxParser<Object> {
    static final SyntaxParser<?> INSTANCE = new NamespaceParser();

    @Override
    public Object accept(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        parser.next(TokenClass.NAMESPACE);
        final Token identifier = parser.next(TokenClass.IDENTIFIER);
        final String namespaceName = identifier.getString();
        final Namespace namespace;

        if (namespaceName.equals(Keywords.THIS)) {
            namespace = context.getDefaultNamespace();
        } else {
            try {
                final QtfLibrary library = context.getMember(namespaceName, MemberType.LIBRARY);
                namespace = library.namespace()
                        .fallback(context.getDefaultNamespace());
            } catch (final QtfException e) {
                throw new QtfParseException(e, identifier.range());
            }
        }

        final boolean skipped = parser.skip(TokenClass.TERMINATOR);
        if (!parser.isNext(TokenClass.OPEN_BRACES)) {
            context.scope().setNamespace(namespace);
            if (!skipped) {
                parser.next(TokenClass.TERMINATOR);
            }
            return null;
        }
        return BlockParser.parseBlock(parser, context, t -> t.copy(namespace));
    }
}
