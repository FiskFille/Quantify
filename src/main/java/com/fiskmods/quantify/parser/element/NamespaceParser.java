package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.Expression;
import com.fiskmods.quantify.parser.tree.NamespaceStatement;
import com.fiskmods.quantify.parser.tree.Statement;

class NamespaceParser implements SyntaxParser<NamespaceStatement> {
    static final NamespaceParser INSTANCE = new NamespaceParser();

    @Override
    public NamespaceStatement accept(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        parser.startTree();
        parser.next(TokenClass.NAMESPACE);
        final Expression expression = ExpressionParser.INSTANCE.accept(parser, context);

        final Statement body;
        final boolean skipped = parser.skip(TokenClass.TERMINATOR);
        if (parser.isNext(TokenClass.OPEN_BRACES)) {
            body = BlockParser.parseBlock(parser);
        } else if (skipped) {
            body = null;
        } else {
            final SyntaxParser<? extends Statement> syntax = SyntaxSelector.selectSyntax(parser.peek());
            body = syntax.accept(parser, context);
        }

        return parser.newNamespaceStatement(expression, body);
    }
}
