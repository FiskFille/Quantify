package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.lexer.Keywords;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.library.QtfLibrary;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.tree.*;

import java.util.Optional;

class IdentifierParser {
    @FunctionalInterface
    interface NameParser<T> {
        T parse(Expression expression, String name, Namespace namespace) throws QtfParseException;
    }

    static Expression parseIdentifier(final QtfParser parser) throws QtfParseException {
        Expression result = Identifier.from(parser.next(TokenClass.IDENTIFIER));

        while (parser.isNext(TokenClass.DOT)) {
            parser.startTree();
            parser.clearPeekedToken();
            final String name = parser.next(TokenClass.IDENTIFIER).getString();
            result = parser.newMemberSelect(result, name);
        }
        return result;
    }

    static <T> T parseIdentifier(final QtfParser parser, final SyntaxContext context, final NameParser<T> nextParser) throws QtfParseException {
        final Expression expression = parseIdentifier(parser);

        if (expression instanceof final MemberSelect memberSelect) {
            final Namespace namespace = getNamespace(context, memberSelect.expression());
            return nextParser.parse(memberSelect, memberSelect.identifier(), namespace);
        } else if (expression instanceof final Identifier identifier) {
            return nextParser.parse(identifier, identifier.name(), context.namespace());
        }
        throw QtfParseException.internal("not an Identifier or MemberSelect: " + expression, expression.range());
    }

    static Namespace getNamespace(final SyntaxContext context, final Expression expression) throws QtfParseException {
        final Namespace namespace;
        final String name;

        if (expression instanceof final MemberSelect memberSelect) {
            namespace = getNamespace(context, memberSelect.expression());
            name = memberSelect.identifier();
        } else if (expression instanceof final Identifier identifier) {
            if (Keywords.THIS.equals(identifier.name())) {
                return context.getDefaultNamespace();
            }
            namespace = context.namespace();
            name = identifier.name();
        } else {
            throw QtfParseException.internal("not an Identifier or MemberSelect: " + expression, expression.range());
        }

        final Optional<QtfLibrary> library = namespace.find(name, MemberType.LIBRARY);
        if (library.isPresent()) {
            return library.get().namespace();
        }

        try {
            return namespace.computeVariable(VarType.STRUCT, name);
        } catch (final QtfException e) {
            throw new QtfParseException(e, expression.range());
        }
    }

    static Statement parseStatementIdentifier(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        return parseIdentifier(parser, context, (expression, name, namespace) -> {
            final Optional<FunctionRef> func = FunctionRefParser.parseFunction(parser, expression, name, namespace);
            if (func.isPresent()) {
                parser.expectLineBreak();
                return ExpressionStatement.of(func.get());
            }

            return AssignmentParser.parseAssignment(parser, context, expression, name, namespace);
        });
    }

    static Expression parseExpressionIdentifier(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        return parseIdentifier(parser, context, (expression, name, namespace) -> {
            final Optional<FunctionRef> func = FunctionRefParser.parseFunction(parser, expression, name, namespace);
            if (func.isPresent()) {
                return func.get();
            }

            try {
                final Optional<Double> constValue = namespace.find(name, MemberType.CONSTANT);
                if (constValue.isPresent()) {
                    return parser.newNumLiteral(constValue.get(), expression.range());
                }

                final VarAddress address = namespace.computeVariable(VarType.NUM, name);
                return parser.newVariableRef(expression, address, false);
            } catch (final QtfException e) {
                throw new QtfParseException(e, expression.range());
            }
        });
    }
}
