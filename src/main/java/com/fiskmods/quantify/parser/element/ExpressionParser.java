package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.Operation;
import com.fiskmods.quantify.parser.tree.Value;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

class ExpressionParser implements SyntaxParser<Value> {
    static final ExpressionParser INSTANCE = new ExpressionParser();

    @Override
    public Value accept(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        final List<Object> stack = new ArrayList<>();
        final Deque<Integer> lastPriority = new ArrayDeque<>();

        stack.add(ExpressionParser.acceptValue(parser, context));

        while (parser.hasNext(QtfParser.Boundary.CLOSURE)) {
            final Operator op = parser.next(TokenClass.OPERATOR).getOperator();
            final Value right = ExpressionParser.acceptValue(parser, context);

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
        return (Value) stack.getFirst();
    }

    private static Value acceptValue(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        final Token peeked = parser.peek();

        // Consumes any leading + or - signs
        if (peeked.type() == TokenClass.OPERATOR) {
            final Operator op = peeked.getOperator();
            if (op == Operator.SUB) {
                parser.clearPeekedToken();
                return acceptValue(parser, context).negate();
            }
            if (op == Operator.ADD) {
                parser.clearPeekedToken();
                return acceptValue(parser, context);
            }
        }
        return switch (peeked.type()) {
            case IDENTIFIER -> IdentifierParser.ANY_VALUE.accept(parser, context);
            case OPEN_PARENTHESIS -> {
                parser.clearPeekedToken();
                final Value val = INSTANCE.accept(parser, context);
                parser.next(TokenClass.CLOSE_PARENTHESIS);
                yield val;
            }
            default -> NumLiteralParser.PARSER.accept(parser, context);
        };
    }

    private static void reduce(final List<Object> stack) {
        final Value right = (Value) stack.removeLast();
        final Operator op = (Operator) stack.removeLast();
        final Value left = (Value) stack.removeLast();
        stack.add(Operation.wrap(left, right, op));
    }
}
