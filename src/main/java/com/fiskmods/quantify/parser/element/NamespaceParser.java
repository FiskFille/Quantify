package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.Keywords;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.library.QtfLibrary;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.Identifier;
import com.fiskmods.quantify.parser.tree.NamespaceStatement;
import com.fiskmods.quantify.parser.tree.Statement;

class NamespaceParser implements SyntaxParser<NamespaceStatement> {
    static final NamespaceParser INSTANCE = new NamespaceParser();

    @Override
    public NamespaceStatement accept(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        parser.startTree();
        parser.next(TokenClass.NAMESPACE);

        final Identifier identifier = Identifier.from(parser.next(TokenClass.IDENTIFIER));
        final Namespace namespace = getNamespace(context, identifier);
        final Statement body;
        final boolean skipped = parser.skip(TokenClass.TERMINATOR);

        if (parser.isNext(TokenClass.OPEN_BRACES)) {
            body = BlockParser.parseBlock(parser, context, t -> t.copy(namespace));
        } else if (skipped) {
            body = null;
            context.scope().setNamespace(namespace);
        } else {
            final SyntaxParser<? extends Statement> syntax = SyntaxSelector.selectSyntax(context, parser.peek());
            context.push(t -> t.copy(namespace));
            body = syntax.accept(parser, context);
            context.pop();
        }

        return parser.newNamespaceStatement(identifier, namespace, body);
    }

    private Namespace getNamespace(final SyntaxContext context, final Identifier identifier) throws QtfParseException {
        if (identifier.name().equals(Keywords.THIS)) {
            return context.getDefaultNamespace();
        }

        try {
            final QtfLibrary library = context.getMember(identifier.name(), MemberType.LIBRARY);
            return library.namespace()
                    .fallback(context.getDefaultNamespace());
        } catch (final QtfException e) {
            throw new QtfParseException(e, identifier.range());
        }
    }
}
