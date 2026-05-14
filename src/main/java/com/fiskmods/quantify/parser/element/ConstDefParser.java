package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.Expression;
import com.fiskmods.quantify.parser.tree.NumLiteral;

class ConstDefParser implements SyntaxParser<Object> {
    static final ConstDefParser INSTANCE = new ConstDefParser();

    @Override
    public Object accept(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        parser.clearPeekedToken();
        final Token identifier = parser.next(TokenClass.IDENTIFIER);
        final Token assignment = parser.next(TokenClass.ASSIGNMENT);

        if (assignment.value() instanceof Operator) {
            throw QtfParseException.error("definitions can't use assignment operators", assignment.range());
        }

        final Expression value = ExpressionParser.INSTANCE.accept(parser, context);
        if (value instanceof final NumLiteral lit) {
            final String name = identifier.getString();
            try {
                context.addMember(name, MemberType.CONSTANT, lit.value());
                return null;
            } catch (final QtfException e) {
                throw new QtfParseException(e, identifier.range());
            }
        }

        throw QtfParseException.error("constants can't be assigned to variables or functions", assignment.range());
    }
}
