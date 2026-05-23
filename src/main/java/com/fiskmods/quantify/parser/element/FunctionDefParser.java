package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.FunctionScope;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.member.Scope;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.*;

import java.util.List;

class FunctionDefParser implements SyntaxParser<FunctionDef> {
    static final SyntaxParser<FunctionDef> PARSER = new FunctionDefParser();

    @Override
    public FunctionDef accept(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        final FunctionDef.DefinedFunctionAddress address = new FunctionDef.DefinedFunctionAddress();

        parser.startTree();
        parser.clearPeekedToken();
        final Identifier identifier = Identifier.from(parser.next(TokenClass.IDENTIFIER));

        try {
            context.addMember(identifier.name(), MemberType.FUNCTION, address);
        } catch (final QtfException e) {
            throw new QtfParseException(e, identifier.range());
        }

        final Scope parentScope = context.scope();
        final FunctionScope scope;

        try {
            scope = FunctionScope.create(parentScope);
        } catch (final QtfException e) {
            throw new QtfParseException(e, identifier.range());
        }

        context.push(scope);

        final List<ParameterTree> parameters = extractParameters(parser);
        final Statement body;
        final FunctionDef.ReturnValueType returnValue;
        address.descriptor = FunctionAddress.descriptor(parameters.size());
        address.parameters = parameters.size();

        if (parser.isNext(TokenClass.ASSIGNMENT)) {
            final Token assignment = parser.next(TokenClass.ASSIGNMENT);
            if (assignment.value() != null) {
                throw QtfParseException.error("function definitions can't use assignment operators", assignment.range());
            }

            final Expression e = ExpressionParser.INSTANCE.accept(parser, context);
            body = parser.newImplicitReturnStatement(e);
            returnValue = FunctionDef.ReturnValueType.IMPLICIT;
        } else {
            body = BlockParser.parseBlock(parser, context);
            returnValue = FunctionDef.ReturnValueType.MISSING;
        }

        context.pop();

        final FunctionDef func = parser.newFunction(identifier, parameters, body, returnValue, address);
        final int index = context.defineFunction(identifier.name(), !parentScope.isInnerScope(), address);

        address.name = createName(identifier.name(), index);
        return func;
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

    static String createName(final String name, final int index) {
        return "$" + index + "_" + name;
    }
}
