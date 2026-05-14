package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.BlockStatement;
import com.fiskmods.quantify.parser.tree.Expression;
import com.fiskmods.quantify.parser.tree.IfStatement;
import com.fiskmods.quantify.parser.tree.Tree;

class IfStatementParser implements SyntaxParser<IfStatement> {
    static final SyntaxParser<IfStatement> PARSER = new IfStatementParser();

    @Override
    public IfStatement accept(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        parser.next(TokenClass.IF);
        parser.next(TokenClass.OPEN_PARENTHESIS);
        final Expression condition = ExpressionParser.INSTANCE.accept(parser, context);
        parser.next(TokenClass.CLOSE_PARENTHESIS);
        parser.skip(TokenClass.TERMINATOR);

        final BlockStatement body = BlockParser.parseBlock(parser, context);
        Tree elseBody = null;
        parser.skip(TokenClass.TERMINATOR);

        if (parser.isNext(TokenClass.ELSE)) {
            parser.clearPeekedToken();
            if (parser.isNext(TokenClass.IF)) {
                elseBody = this.accept(parser, context);
            } else {
                elseBody = BlockParser.parseBlock(parser, context);
            }
        }
        return new IfStatement(condition, body, elseBody);
    }
}
