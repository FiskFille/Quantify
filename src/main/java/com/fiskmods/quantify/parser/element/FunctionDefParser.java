package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.FunctionScope;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.member.Scope;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.FunctionDef;

import java.util.HashSet;
import java.util.Set;

class FunctionDefParser implements SyntaxParser<JvmFunction> {
    static final SyntaxParser<JvmFunction> PARSER = new FunctionDefParser();

    @Override
    public JvmFunction accept(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        final FunctionDef.DefinedFunctionAddress address = new FunctionDef.DefinedFunctionAddress();

        parser.clearPeekedToken();
        final Token identifier = parser.next(TokenClass.IDENTIFIER);
        final String name = identifier.getString();

        try {
            context.addMember(name, MemberType.FUNCTION, address);
        } catch (final QtfException e) {
            throw new QtfParseException(e, identifier.range());
        }

        parser.next(TokenClass.OPEN_PARENTHESIS);
        final String[] parameters = parseParameters(parser);
        final Scope parentScope = context.scope();
        final FunctionScope scope;
        final JvmFunction body;
        final FunctionDef.ReturnValueType returnValue;

        try {
            scope = FunctionScope.create(parentScope, parameters);
        } catch (final QtfException e) {
            throw new QtfParseException(e, identifier.range());
        }

        address.descriptor = FunctionAddress.descriptor(parameters.length);
        address.parameters = parameters.length;

        if (parser.isNext(TokenClass.ASSIGNMENT)) {
            final Token assignment = parser.next(TokenClass.ASSIGNMENT);
            if (assignment.value() != null) {
                throw QtfParseException.error("function definitions can't use assignment operators", assignment.range());
            }

            context.push(scope);
            body = ExpressionParser.INSTANCE.accept(parser, context);
            returnValue = FunctionDef.ReturnValueType.IMPLICIT;
            context.pop();
        } else {
            body = new FunctionBodyParser(scope).accept(parser, context);
            returnValue = scope.hasReturnValue() ? FunctionDef.ReturnValueType.EXPLICIT : FunctionDef.ReturnValueType.MISSING;
        }

        final boolean isVisible = !parentScope.isInnerScope();
        final int index = context.defineFunction(new FunctionDef(name, isVisible, address, body, returnValue));
        address.name = createName(name, index);
        return null;
    }

    private String[] parseParameters(final QtfParser parser) throws QtfParseException {
        // Function has no parameters
        if (parser.isNext(TokenClass.CLOSE_PARENTHESIS)) {
            parser.clearPeekedToken();
            return new String[0];
        }

        final Set<String> set = new HashSet<>();
        while (true) {
            final Token token = parser.next(TokenClass.IDENTIFIER);
            if (!set.add(token.getString())) {
                throw QtfParseException.error("duplicate parameter '%s'".formatted(token.getString()), token.range());
            }

            if (parser.isNext(TokenClass.COMMA)) {
                parser.clearPeekedToken();
                continue;
            }
            break;
        }

        parser.next(TokenClass.CLOSE_PARENTHESIS);
        return set.toArray(new String[0]);
    }

    static String createName(final String name, final int index) {
        return "$" + index + "_" + name;
    }
}
