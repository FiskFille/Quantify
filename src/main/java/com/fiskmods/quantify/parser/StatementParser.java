package com.fiskmods.quantify.parser;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.*;
import com.fiskmods.quantify.parser.tree.*;

import java.util.ArrayList;
import java.util.List;

class StatementParser {
    private final QtfParser parser;
    private final TokenStream tokens;
    private final ExpressionParser expr;

    StatementParser(final QtfParser parser, final TokenStream tokens, final ExpressionParser expr) {
        this.parser = parser;
        this.tokens = tokens;
        this.expr = expr;
    }

    Statement parseStatement() throws QtfParseException {
        return switch (tokens.peek().type()) {
            case IMPORT -> parseImport();
            case INPUT -> parseInput();
            case PUBLIC -> parseVar(true);
            case VAR -> parseVar(false);
            case CONST -> parseConst();
            case IF -> parseIf();
            case INTERPOLATE -> parseInterpolate();
            case NAMESPACE -> parseNamespace();
            case FUNC -> parseFuncDef();
            case RETURN -> parseReturn();
            default ->  {
                final Expression expression = expr.parseIdentifier();

                if (tokens.isNext(TokenClass.OPEN_PARENTHESIS)) {
                    final FunctionRef func = expr.parseFunction(expression);
                    tokens.expectLineBreak();
                    yield ExpressionStatement.of(func);
                }

                final Statement statement = parseAssignment(expression);
                tokens.expectLineBreak();
                yield statement;
            }
        };
    }

    ImportStatement parseImport() throws QtfParseException {
        parser.startTree();
        tokens.next(TokenClass.IMPORT);
        final String key = tokens.next(TokenClass.STR_LITERAL).getString();
        tokens.next(TokenClass.COLON);
        final String name = tokens.next(TokenClass.IDENTIFIER).getString();

        final ImportStatement statement = parser.newImportStatement(name, key);
        tokens.expectLineBreak();
        return statement;
    }

    InputStatement parseInput() throws QtfParseException {
        parser.startTree();
        tokens.next(TokenClass.INPUT);
        tokens.next(TokenClass.OPEN_BRACKETS);
        final int index = tokens.next(TokenClass.NUM_LITERAL).getNumber().intValue();
        tokens.next(TokenClass.CLOSE_BRACKETS);
        tokens.next(TokenClass.COLON);

        final String name = tokens.next(TokenClass.IDENTIFIER).getString();
        final InputStatement statement = parser.newInput(index, name);
        tokens.expectLineBreak();
        return statement;
    }

    VarDefinitionTree parseVar(final boolean isPublic) throws QtfParseException {
        parser.startTree();

        if (isPublic)
            tokens.next(TokenClass.PUBLIC);
        tokens.next(TokenClass.VAR);

        final List<Token> identifiers = TokenList.parseNonEmpty(tokens, TokenClass.IDENTIFIER, TokenClass.COMMA);
        final Identifier type = expr.parseType();
        final Expression initializer = tokens.consume(TokenClass.ASSIGNMENT)
                ? expr.parseExpression() : null;

        final List<String> names = new ArrayList<>(identifiers.size());
        for (final Token token : identifiers) {
            names.add(token.getString());
        }

        return parser.newVariable(names, type, initializer, isPublic);
    }

    ConstDefinitionTree parseConst() throws QtfParseException {
        parser.startTree();
        tokens.next(TokenClass.CONST);
        final String name = tokens.next(TokenClass.IDENTIFIER).getString();
        final Identifier type = expr.parseType();

        tokens.next(TokenClass.ASSIGNMENT);

        final Expression value = expr.parseExpression();
        return parser.newConst(name, type, value);
    }

    Statement parseAssignment(final Expression expression) throws QtfParseException {
        final VarRef firstVar = parser.newVariableRef(expression, false);
        final List<VarRef> targets;

        if (tokens.consume(TokenClass.COMMA)) {
            targets = new ArrayList<>();
            targets.add(firstVar);

            do {
                targets.add(expr.parseVariable());
            } while (tokens.consume(TokenClass.COMMA));
        } else {
            targets = List.of(firstVar);
        }

        parser.startTree();
        return switch (tokens.peek().type()) {
            case COMPOUND_ASSIGN -> {
                final Operator op = tokens.next(TokenClass.COMPOUND_ASSIGN).getOperator();
                final Expression value = expr.parseExpression();
                yield parser.newCompoundAssignment(targets, value, op);
            }
            case LERP, LERP_ROT -> {
                final boolean rotational = tokens.next().type() == TokenClass.LERP_ROT;
                final Expression value = expr.parseExpression();
                yield parser.newLerpAssignment(targets, value, rotational);
            }
            default -> {
                tokens.next(TokenClass.ASSIGNMENT);
                final Expression value = expr.parseExpression();
                yield parser.newAssignment(targets, value);
            }
        };
    }

    BlockStatement parseBlock() throws QtfParseException {
        parser.startTree();
        tokens.next(TokenClass.OPEN_BRACES);
        final var statements = parser.parse(true);
        tokens.next(TokenClass.CLOSE_BRACES);
        return parser.newBlockStatement(statements);
    }

    IfStatement parseIf() throws QtfParseException {
        parser.startTree();
        tokens.next(TokenClass.IF);
        final Expression condition = expr.parseExpression();
        tokens.skip(TokenClass.TERMINATOR);

        final BlockStatement body = parseBlock();
        Statement elseBody = null;
        tokens.skip(TokenClass.TERMINATOR);

        if (tokens.consume(TokenClass.ELSE)) {
            if (tokens.isNext(TokenClass.IF)) {
                elseBody = parseIf();
            } else {
                elseBody = parseBlock();
            }
        }
        return parser.newIfStatement(condition, body, elseBody);
    }

    InterpolateStatement parseInterpolate() throws QtfParseException {
        parser.startTree();
        tokens.next(TokenClass.INTERPOLATE);
        final Expression progress = expr.parseExpression();
        tokens.skip(TokenClass.TERMINATOR);

        final BlockStatement body = parseBlock();
        return parser.newInterpolateStatement(progress, body);
    }

    NamespaceStatement parseNamespace() throws QtfParseException {
        parser.startTree();
        tokens.next(TokenClass.NAMESPACE);
        final Expression expression = expr.parseExpression();

        final Statement body;
        final boolean skipped = tokens.skip(TokenClass.TERMINATOR);
        if (tokens.isNext(TokenClass.OPEN_BRACES)) {
            body = parseBlock();
        } else {
            body = skipped ? null : parseStatement();
        }

        return parser.newNamespaceStatement(expression, body);
    }

    FunctionDef parseFuncDef() throws QtfParseException {
        parser.startTree();
        tokens.next(TokenClass.FUNC);
        final String name = tokens.next(TokenClass.IDENTIFIER).getString();

        final List<ParameterTree> parameters = parseParameterList();
        final Statement body;
        final FunctionDef.ReturnValueType returnValue;

        if (tokens.consume(TokenClass.ASSIGNMENT)) {
            final Expression e = expr.parseExpression();
            body = parser.newImplicitReturnStatement(e);
            returnValue = FunctionDef.ReturnValueType.IMPLICIT;
        } else {
            body = parseBlock();
            returnValue = FunctionDef.ReturnValueType.MISSING;
        }

        return parser.newFunction(name, parameters, body, returnValue);
    }

    List<ParameterTree> parseParameterList() throws QtfParseException {
        tokens.next(TokenClass.OPEN_PARENTHESIS);

        // Function has no parameters
        if (tokens.consume(TokenClass.CLOSE_PARENTHESIS)) {
            return List.of();
        }

        final List<ParameterTree> parameters = new ArrayList<>();
        do {
            parameters.add(parseParameter());
        } while (tokens.consume(TokenClass.COMMA));

        tokens.next(TokenClass.CLOSE_PARENTHESIS);
        return parameters;
    }

    ParameterTree parseParameter() throws QtfParseException {
        parser.startTree();
        final String name = tokens.next(TokenClass.IDENTIFIER).getString();
        final Identifier type = expr.parseType();

        return parser.newParameter(name, type);
    }

    ReturnStatement parseReturn() throws QtfParseException {
        parser.startTree();
        tokens.next(TokenClass.RETURN);
        final Expression e = expr.parseExpression();
        tokens.skip(TokenClass.TERMINATOR);

        // Intentionally trigger exception if there are more tokens after return value
        if (!tokens.isNext(TokenClass.CLOSE_BRACES)) {
            tokens.next(TokenClass.CLOSE_BRACES);
        }
        return parser.newReturnStatement(e);
    }
}
