package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.BlockStatement;
import com.fiskmods.quantify.parser.tree.Expression;
import com.fiskmods.quantify.parser.tree.InterpolateStatement;

class InterpolateStatementParser implements SyntaxParser<InterpolateStatement> {
    static final SyntaxParser<InterpolateStatement> PARSER = new InterpolateStatementParser();

    @Override
    public InterpolateStatement accept(final QtfParser parser) throws QtfParseException {
        parser.startTree();
        parser.next(TokenClass.INTERPOLATE);
        final Expression progress = ExpressionParser.INSTANCE.accept(parser);
        parser.skip(TokenClass.TERMINATOR);

        final BlockStatement body = BlockParser.parseBlock(parser);
        return parser.newInterpolateStatement(progress, body);
    }
}
