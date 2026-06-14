package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.Expression;
import com.fiskmods.quantify.parser.tree.Operation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

class ExpressionParser implements SyntaxParser<Expression> {
    static final ExpressionParser INSTANCE = new ExpressionParser();

    @Override
    public Expression accept(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        final List<Object> stack = new ArrayList<>();
        final Deque<Integer> lastPriority = new ArrayDeque<>();

        stack.add(ExpressionParser.acceptValue(parser, context));

        while (parser.isNext(TokenClass.OPERATOR)) {
            final Operator op = parser.next(TokenClass.OPERATOR).getOperator();
            final Expression right = ExpressionParser.acceptValue(parser, context);

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

    static Expression parseParensExpression(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        parser.next(TokenClass.OPEN_PARENTHESIS);
        final Expression e = INSTANCE.accept(parser, context);
        parser.next(TokenClass.CLOSE_PARENTHESIS);
        return e;
    }

    static Expression parseBraceExpression(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        parser.next(TokenClass.OPEN_BRACES);
        parser.skip(TokenClass.TERMINATOR);
        final Expression e = INSTANCE.accept(parser, context);
        parser.skip(TokenClass.TERMINATOR);
        parser.next(TokenClass.CLOSE_BRACES);
        return e;
    }

    private static Expression acceptValue(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        final Token peeked = parser.peek();

        // Consumes any leading + or - signs
        if (peeked.type() == TokenClass.OPERATOR) {
            final Operator op = peeked.getOperator();
            if (op == Operator.SUB) {
                parser.clearPeekedToken();
                final Expression e = acceptValue(parser, context);
                return Expression.negate(e);
            }
            if (op == Operator.ADD) {
                parser.clearPeekedToken();
                return acceptValue(parser, context);
            }
        }
        return switch (peeked.type()) {
            case IDENTIFIER -> IdentifierParser.parseExpressionIdentifier(parser);
            case OPEN_PARENTHESIS -> parseParensExpression(parser, context);
            case OPEN_BRACES -> parseBraceExpression(parser, context);
            case IF -> IfElseParser.INSTANCE.accept(parser, context);
            default -> NumLiteralParser.PARSER.accept(parser, context);
        };
    }

    private static void reduce(final List<Object> stack) {
        final Expression right = (Expression) stack.removeLast();
        final Operator op = (Operator) stack.removeLast();
        final Expression left = (Expression) stack.removeLast();
        stack.add(Operation.wrap(left, right, op));
    }
}
