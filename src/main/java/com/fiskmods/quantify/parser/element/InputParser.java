package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.Identifier;
import com.fiskmods.quantify.parser.tree.InputStatement;
import com.fiskmods.quantify.parser.tree.VarRef;

class InputParser implements SyntaxParser<InputStatement> {
    static final InputParser INSTANCE = new InputParser();

    @Override
    public InputStatement accept(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        parser.startTree();
        parser.clearPeekedToken();
        parser.next(TokenClass.OPEN_BRACKETS);
        final int index = parser.next(TokenClass.NUM_LITERAL).getNumber().intValue();
        parser.next(TokenClass.CLOSE_BRACKETS);
        parser.next(TokenClass.COLON);

        final Identifier identifier = Identifier.from(parser.next(TokenClass.IDENTIFIER));
        final VarAddress inputAddress;
        final VarAddress targetAddress;

        try {
            inputAddress = context.addInputVariable(identifier.name(), index);
            targetAddress = context.scope().addLocalVariable(identifier.name());
        } catch (final QtfException e) {
            throw new QtfParseException(e, identifier.range());
        }

        final VarRef inputVar = parser.newVariableRef(identifier, inputAddress, false);
        final InputStatement statement = parser.newInput(index, identifier, inputVar, targetAddress);
        parser.expectLineBreak();
        return statement;
    }
}
