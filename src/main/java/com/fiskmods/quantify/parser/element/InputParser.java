package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.Assignment;

class InputParser implements SyntaxParser<Assignment> {
    static final InputParser INSTANCE = new InputParser();

    @Override
    public Assignment accept(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        parser.clearPeekedToken();
        parser.next(TokenClass.OPEN_BRACKETS);
        final int index = parser.next(TokenClass.NUM_LITERAL).getNumber().intValue();
        parser.next(TokenClass.CLOSE_BRACKETS);
        parser.next(TokenClass.COLON);

        final Token identifier = parser.next(TokenClass.IDENTIFIER);
        final String name = identifier.getString();
        final VarAddress var;
        final VarAddress inputVar;

        try {
            var = context.addLocalVariable(name);
            inputVar = context.addInputVariable(name, index);
        } catch (final QtfException e) {
            throw new QtfParseException(e, identifier.range());
        }

        parser.expectLineBreak();
        return new Assignment.AbsoluteAssignment(var, inputVar, null);
    }
}
