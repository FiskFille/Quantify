package com.fiskmods.quantify.parser;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.lexer.token.TokenStream;
import com.fiskmods.quantify.parser.tree.*;
import org.jspecify.annotations.Nullable;

import java.util.*;

class ExpressionParser {
    private final QtfParser parser;
    private final TokenStream tokens;

    ExpressionParser(final QtfParser parser, final TokenStream tokens) {
        this.parser = parser;
        this.tokens = tokens;
    }

    Expression parseOperand() throws QtfParseException {
        final Token peeked = tokens.peek();

        // Consumes any leading + or - signs
        if (peeked.type() == TokenClass.OPERATOR) {
            final Operator op = peeked.getOperator();
            if (op == Operator.SUB) {
                tokens.clearPeekedToken();
                final Expression e = parseOperand();
                return Expression.negate(e);
            }
            if (op == Operator.ADD) {
                tokens.clearPeekedToken();
                return parseOperand();
            }
        }
        return switch (peeked.type()) {
            case IDENTIFIER -> {
                final Expression expression = parseIdentifier();
                if (tokens.isNext(TokenClass.OPEN_PARENTHESIS)) {
                    yield parseFunction(expression);
                }
                yield expression;
            }
            case OPEN_PARENTHESIS -> {
                tokens.next(TokenClass.OPEN_PARENTHESIS);
                final Expression expression = parseExpression();
                tokens.next(TokenClass.CLOSE_PARENTHESIS);
                yield expression;
            }
            case OPEN_BRACES -> parseBraceExpression();
            case IF -> parseIfThenElse();
            default -> parseNum();
        };
    }

    Expression parseExpression() throws QtfParseException {
        final List<Object> stack = new ArrayList<>();
        final Deque<Integer> lastPriority = new ArrayDeque<>();

        stack.add(parseOperand());

        while (tokens.isNext(TokenClass.OPERATOR)) {
            final Operator op = tokens.next(TokenClass.OPERATOR).getOperator();
            final Expression right = parseOperand();

            while (!lastPriority.isEmpty() && lastPriority.peek() <= op.priority()) {
                reduce(stack);
                lastPriority.pop();
            }
            stack.add(op);
            stack.add(right);
            lastPriority.push(op.priority());
        }

        while (stack.size() > 1) {
            reduce(stack);
        }
        return (Expression) stack.getFirst();
    }

    static void reduce(final List<Object> stack) {
        final Expression right = (Expression) stack.removeLast();
        final Operator op = (Operator) stack.removeLast();
        final Expression left = (Expression) stack.removeLast();
        stack.add(Operation.wrap(left, right, op));
    }

    Expression parseBraceExpression() throws QtfParseException {
        tokens.next(TokenClass.OPEN_BRACES);
        tokens.skip(TokenClass.TERMINATOR);
        final Expression e = parseExpression();
        tokens.skip(TokenClass.TERMINATOR);
        tokens.next(TokenClass.CLOSE_BRACES);
        return e;
    }

    NumLiteral parseNum() throws QtfParseException {
        parser.startTree();
        double value = tokens.next(TokenClass.NUM_LITERAL).getNumber().doubleValue();
        if (tokens.consume(TokenClass.DEGREES)) {
            value *= Math.PI / 180;
        }
        return parser.newNumLiteral(value);
    }

    Expression parseIdentifier() throws QtfParseException {
        Expression result = Identifier.from(tokens.next(TokenClass.IDENTIFIER));

        while (tokens.isNext(TokenClass.DOT)) {
            parser.startTree();
            tokens.clearPeekedToken();
            final String name = tokens.next(TokenClass.IDENTIFIER).getString();
            result = parser.newMemberSelect(result, name);
        }
        return result;
    }

    FunctionRef parseFunction(final Expression selector) throws QtfParseException {
        parser.startTree();
        tokens.next(TokenClass.OPEN_PARENTHESIS);

        if (tokens.consume(TokenClass.CLOSE_PARENTHESIS)) {
            return parser.newFunctionRef(selector, List.of());
        }

        final List<Expression> args = new ArrayList<>();
        do {
            args.add(parseExpression());
        } while (tokens.consume(TokenClass.COMMA));

        tokens.next(TokenClass.CLOSE_PARENTHESIS);
        return parser.newFunctionRef(selector, args);
    }

    VarRef parseVariable() throws QtfParseException {
        final boolean isNegated = tokens.consume(TokenClass.OPERATOR, Operator.SUB);
        final Expression expression = parseIdentifier();
        return parser.newVariableRef(expression, isNegated);
    }

    @Nullable Identifier parseType() throws QtfParseException {
        if (tokens.consume(TokenClass.COLON)) {
            return Identifier.from(tokens.next(TokenClass.IDENTIFIER));
        }
        return null;
    }

    IfElseExpression parseIfThenElse() throws QtfParseException {
        parser.startTree();
        tokens.next(TokenClass.IF);
        final Expression condition = parseExpression();
        tokens.skip(TokenClass.TERMINATOR);

        final boolean bracesOptional = tokens.consume(TokenClass.THEN);
        final Expression thenExpression = bracesOptional ? parseExpression() : parseBraceExpression();
        tokens.skip(TokenClass.TERMINATOR);
        tokens.next(TokenClass.ELSE);
        final Expression elseExpression = bracesOptional ? parseExpression() : parseBraceExpression();

        return parser.newIfElse(condition, thenExpression, elseExpression);
    }
}
