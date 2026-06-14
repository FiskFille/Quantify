package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.tree.Expression;
import com.fiskmods.quantify.parser.tree.Statement;
import com.fiskmods.quantify.parser.tree.VarRef;

import java.util.ArrayList;
import java.util.List;

class AssignmentParser {
    static Statement parseAssignment(final QtfParser parser, final Expression expression) throws QtfParseException {
        final VarRef firstVar = parser.newVariableRef(expression, false);
        final List<VarRef> targets;

        if (parser.consume(TokenClass.COMMA)) {
            targets = new ArrayList<>();
            targets.add(firstVar);
            parser.nextSequence(VariableParser::parseVariable, TokenClass.COMMA, targets);
        } else {
            targets = List.of(firstVar);
        }

        parser.startTree();
        return switch (parser.peek().type()) {
            case COMPOUND_ASSIGN -> {
                final Operator op = parser.next(TokenClass.COMPOUND_ASSIGN).getOperator();
                final Expression value = ExpressionParser.INSTANCE.accept(parser);
                yield parser.newCompoundAssignment(targets, value, op);
            }
            case LERP, LERP_ROT -> {
                final boolean rotational = parser.next().type() == TokenClass.LERP_ROT;
                final Expression value = ExpressionParser.INSTANCE.accept(parser);
                yield parser.newLerpAssignment(targets, value, rotational);
            }
            default -> {
                parser.next(TokenClass.ASSIGNMENT);
                final Expression value = ExpressionParser.INSTANCE.accept(parser);
                yield parser.newAssignment(targets, value);
            }
        };
    }
}
