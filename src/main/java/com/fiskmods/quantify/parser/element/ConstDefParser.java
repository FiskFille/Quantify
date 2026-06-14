package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.ConstDefinitionTree;
import com.fiskmods.quantify.parser.tree.Expression;
import com.fiskmods.quantify.parser.tree.Identifier;

class ConstDefParser implements SyntaxParser<ConstDefinitionTree> {
    static final ConstDefParser INSTANCE = new ConstDefParser();

    @Override
    public ConstDefinitionTree accept(final QtfParser parser) throws QtfParseException {
        parser.startTree();
        parser.next(TokenClass.CONST);
        final String name = parser.next(TokenClass.IDENTIFIER).getString();
        final Identifier type = TypeParser.parseType(parser);

        final Token assignment = parser.next(TokenClass.ASSIGNMENT);
        if (assignment.value() instanceof Operator) {
            throw QtfParseException.error("definitions can't use assignment operators", assignment.range());
        }

        final Expression value = ExpressionParser.INSTANCE.accept(parser);
        return parser.newConst(name, type, value);
    }
}
