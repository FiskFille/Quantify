package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.Assignment;
import com.fiskmods.quantify.parser.tree.Identifier;
import com.fiskmods.quantify.parser.tree.VarRef;

import java.util.List;

class InputParser implements SyntaxParser<Assignment> {
    static final InputParser INSTANCE = new InputParser();

    @Override
    public Assignment accept(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        parser.startTree();
        parser.clearPeekedToken();
        parser.next(TokenClass.OPEN_BRACKETS);
        final int index = parser.next(TokenClass.NUM_LITERAL).getNumber().intValue();
        parser.next(TokenClass.CLOSE_BRACKETS);
        parser.next(TokenClass.COLON);

        final Identifier identifier = Identifier.from(parser.next(TokenClass.IDENTIFIER));
        final VarRef var;
        final VarRef inputVar;

        try {
            var = parser.newVariableRef(identifier, context.scope().addLocalVariable(identifier.name()), false);
            inputVar = parser.newVariableRef(identifier, context.addInputVariable(identifier.name(), index), false);
        } catch (final QtfException e) {
            throw new QtfParseException(e, identifier.range());
        }

        final Assignment assignment = parser.newAssignment(List.of(var), inputVar, null);
        parser.expectLineBreak();
        return assignment;
    }
}
