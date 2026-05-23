package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.Identifier;
import com.fiskmods.quantify.parser.tree.ParameterTree;

class ParameterParser implements SyntaxParser<ParameterTree> {
    static final ParameterParser INSTANCE = new ParameterParser();

    @Override
    public ParameterTree accept(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        parser.startTree();
        final Identifier name = Identifier.from(parser.next(TokenClass.IDENTIFIER));
        final VarType<?> type = extractType(parser);

        try {
            final VarAddress address = type.defineLocal(name.name(), context.scope());
            return parser.newParameter(name, type, address);
        } catch (final QtfException e) {
            throw new QtfParseException(e, name.range());
        }
    }

    private VarType<?> extractType(final QtfParser parser) throws QtfParseException {
        if (!parser.isNext(TokenClass.COLON)) {
            return VarType.NUM;
        }

        parser.clearPeekedToken();
        final Token identifier = parser.next(TokenClass.IDENTIFIER);
        final String typeName = identifier.getString();
        final VarType<?> type;
        try {
            type = VarType.getType(typeName);
        } catch (final QtfException e) {
            throw new QtfParseException(e, identifier.range());
        }

        if (type != VarType.NUM) {
            throw QtfParseException.error("parameters only allow num type", identifier.range());
        }
        return type;
    }
}
