package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.IfStatement;
import com.fiskmods.quantify.parser.tree.StatementBody;
import com.fiskmods.quantify.parser.tree.Value;

class IfStatementParser implements SyntaxParser<IfStatement> {
    static final SyntaxParser<IfStatement> PARSER = new IfStatementParser();

    @Override
    public IfStatement accept(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        parser.next(TokenClass.IF);
        parser.next(TokenClass.OPEN_PARENTHESIS);
        final Value condition = ExpressionParser.INSTANCE.accept(parser, context);
        parser.next(TokenClass.CLOSE_PARENTHESIS);
        parser.skip(TokenClass.TERMINATOR);

        final StatementBody body = StatementBodyParser.PARSER.accept(parser, context);
        JvmFunction elseBody = null;
        parser.skip(TokenClass.TERMINATOR);

        if (parser.isNext(TokenClass.ELSE)) {
            parser.clearPeekedToken();
            if (parser.isNext(TokenClass.IF)) {
                elseBody = this.accept(parser, context);
            } else {
                elseBody = StatementBodyParser.PARSER.accept(parser, context);
            }
        }
        return new IfStatement(condition, body, elseBody);
    }
}
