package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.BlockStatement;
import com.fiskmods.quantify.parser.tree.Expression;
import com.fiskmods.quantify.parser.tree.IfStatement;
import com.fiskmods.quantify.parser.tree.Statement;

class IfStatementParser implements SyntaxParser<IfStatement> {
    static final SyntaxParser<IfStatement> PARSER = new IfStatementParser();

    @Override
    public IfStatement accept(final QtfParser parser) throws QtfParseException {
        parser.startTree();
        parser.next(TokenClass.IF);
        final Expression condition = ExpressionParser.INSTANCE.accept(parser);
        parser.skip(TokenClass.TERMINATOR);

        final BlockStatement body = BlockParser.parseBlock(parser);
        Statement elseBody = null;
        parser.skip(TokenClass.TERMINATOR);

        if (parser.isNext(TokenClass.ELSE)) {
            parser.clearPeekedToken();
            if (parser.isNext(TokenClass.IF)) {
                elseBody = accept(parser);
            } else {
                elseBody = BlockParser.parseBlock(parser);
            }
        }
        return parser.newIfStatement(condition, body, elseBody);
    }
}
