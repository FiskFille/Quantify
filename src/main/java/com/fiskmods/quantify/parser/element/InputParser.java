package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.Assignment;
import com.fiskmods.quantify.parser.tree.VarRef;

import java.util.List;

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
        final VarRef var;
        final VarRef inputVar;

        try {
            var = new VarRef(context.addLocalVariable(name), false);
            inputVar = new VarRef(context.addInputVariable(name, index), false);
        } catch (final QtfException e) {
            throw new QtfParseException(e, identifier.range());
        }

        parser.expectLineBreak();
        return new Assignment(List.of(var), inputVar, null);
    }
}
