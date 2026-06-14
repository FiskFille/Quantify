package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.Expression;
import com.fiskmods.quantify.parser.tree.FunctionDef;
import com.fiskmods.quantify.parser.tree.ParameterTree;
import com.fiskmods.quantify.parser.tree.Statement;

import java.util.List;

class FunctionDefParser implements SyntaxParser<FunctionDef> {
    static final SyntaxParser<FunctionDef> PARSER = new FunctionDefParser();

    @Override
    public FunctionDef accept(final QtfParser parser) throws QtfParseException {
        parser.startTree();
        parser.clearPeekedToken();
        final String name = parser.next(TokenClass.IDENTIFIER).getString();

        final List<ParameterTree> parameters = extractParameters(parser);
        final Statement body;
        final FunctionDef.ReturnValueType returnValue;

        if (parser.isNext(TokenClass.ASSIGNMENT)) {
            final Token assignment = parser.next(TokenClass.ASSIGNMENT);
            if (assignment.value() != null) {
                throw QtfParseException.error("function definitions can't use assignment operators", assignment.range());
            }

            final Expression e = ExpressionParser.INSTANCE.accept(parser);
            body = parser.newImplicitReturnStatement(e);
            returnValue = FunctionDef.ReturnValueType.IMPLICIT;
        } else {
            body = BlockParser.parseBlock(parser);
            returnValue = FunctionDef.ReturnValueType.MISSING;
        }

        return parser.newFunction(name, parameters, body, returnValue);
    }

    private List<ParameterTree> extractParameters(final QtfParser parser) throws QtfParseException {
        parser.next(TokenClass.OPEN_PARENTHESIS);

        // Function has no parameters
        if (parser.isNext(TokenClass.CLOSE_PARENTHESIS)) {
            parser.clearPeekedToken();
            return List.of();
        }

        final List<ParameterTree> parameters = parser.nextSequence(ParameterParser.INSTANCE, TokenClass.COMMA);
        parser.next(TokenClass.CLOSE_PARENTHESIS);
        return parameters;
    }
}
