package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.jvm.JvmFunctionDefinition;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.FunctionScope;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.member.Scope;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.Expression;
import com.fiskmods.quantify.parser.tree.FunctionDef;
import com.fiskmods.quantify.parser.tree.Identifier;
import com.fiskmods.quantify.parser.tree.Statement;

import java.util.HashSet;
import java.util.Set;

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

        parser.next(TokenClass.OPEN_PARENTHESIS);
        final String[] parameters = parseParameters(parser);
        final Scope parentScope = context.scope();
        final FunctionScope scope;
        final Statement body;
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
            final Expression e = ExpressionParser.INSTANCE.accept(parser, context);
            body = parser.newImplicitReturnStatement(e);
            returnValue = FunctionDef.ReturnValueType.IMPLICIT;
            context.pop();
        } else {
            body = BlockParser.parseBlock(parser, context, scope);
            returnValue = scope.hasReturnValue() ? FunctionDef.ReturnValueType.EXPLICIT : FunctionDef.ReturnValueType.MISSING;
        }

        final FunctionDef func = parser.newFunction(identifier, address, body, returnValue);

        final boolean isVisible = !parentScope.isInnerScope();
        final int index = context.defineFunction(new JvmFunctionDefinition(identifier.name(), isVisible, address));
        address.name = createName(identifier.name(), index);

        return func;
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
