package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.tree.Expression;
import com.fiskmods.quantify.parser.tree.Statement;
import com.fiskmods.quantify.parser.tree.VarRef;

import java.util.List;

class AssignmentParser {
    private static Statement parseAssignment(final QtfParser parser, final List<VarRef> targets) throws QtfParseException {
        parser.startTree();
        final Token assignment = parser.next(TokenClass.ASSIGNMENT);
        final Operator op = assignment.getAssignmentOperator(false);

        final Expression value = ExpressionParser.INSTANCE.accept(parser);
        if (op == Operator.LERP || op == Operator.LERP_ROT) {
            return parser.newLerpAssignment(targets, value, op == Operator.LERP_ROT);
        } else {
            return parser.newAssignment(targets, value, op);
        }
    }

    static Statement parseAssignment(final QtfParser parser, final Expression expression) throws QtfParseException {
        final VarRef firstVar = parser.newVariableRef(expression, false);

        if (parser.isNext(TokenClass.COMMA)) {
            final List<VarRef> list = VariableParser.parseList(parser, firstVar);
            return parseAssignment(parser, list);
        } else {
            return parseAssignment(parser, List.of(firstVar));
        }
    }
}
