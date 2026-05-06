package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.Assignable;
import com.fiskmods.quantify.parser.tree.Assignment;
import com.fiskmods.quantify.parser.tree.Value;
import com.fiskmods.quantify.parser.tree.VariableList;

class AssignmentParser {
    public static SyntaxParser<Assignment> parser(final Assignable target, final boolean isDefinition) {
        return (parser, context) -> {
            // Empty definition
            if (!parser.hasNext(QtfParser.Boundary.LINE)) {
                return new Assignment.AbsoluteAssignment(target, null, null);
            }

            final Token assignment = parser.next(TokenClass.ASSIGNMENT);
            final Operator op = assignment.getAssignmentOperator(context, isDefinition);

            final Value value = ExpressionParser.INSTANCE.accept(parser, context);
            if (op == Operator.LERP || op == Operator.LERP_ROT) {
                return new Assignment.LerpAssignment(target, value, context.scope().getLerpProgress(), op == Operator.LERP_ROT);
            }
            return new Assignment.AbsoluteAssignment(target, value, op);
        };
    }

    public static SyntaxParser<Assignment> parserFrom(final String name, final Token.Range range, final Namespace namespace) {
        return (parser, context) -> {
            final VarAddress firstVar = VariableParser.compute(name, range, namespace, VarType.NUM, 0);

            if (parser.isNext(TokenClass.COMMA)) {
                final VariableList<?> list = VariableParser.parseList(firstVar, 0).accept(parser, context);
                return parser(list, false).accept(parser, context);
            }
            return parser(firstVar, false).accept(parser, context);
        };
    }
}
