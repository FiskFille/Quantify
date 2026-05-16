package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.tree.Expression;
import com.fiskmods.quantify.parser.tree.Statement;
import com.fiskmods.quantify.parser.tree.VarRef;

import java.util.List;

class AssignmentParser {
    private static Statement parseAssignment(final QtfParser parser, final SyntaxContext context, final List<? extends VarRef> targets) throws QtfParseException {
        parser.startTree();
        final Token assignment = parser.next(TokenClass.ASSIGNMENT);
        final Operator op = assignment.getAssignmentOperator(context, false);

        final Expression value = ExpressionParser.INSTANCE.accept(parser, context);
        if (op == Operator.LERP || op == Operator.LERP_ROT) {
            return parser.newLerpAssignment(targets, value, context.scope().getLerpProgress(), op == Operator.LERP_ROT);
        } else {
            return parser.newAssignment(targets, value, op);
        }
    }

    static Statement parseAssignment(final QtfParser parser, final SyntaxContext context, final String name, final Token.Range range, final Namespace namespace) throws QtfParseException {
        final VarRef firstVar = VariableParser.compute(parser, name, range, namespace, VarType.NUM, false);

        if (parser.isNext(TokenClass.COMMA)) {
            final List<? extends VarRef> list = VariableParser.parseList(parser, context, firstVar);
            return parseAssignment(parser, context, list);
        } else {
            return parseAssignment(parser, context, List.of(firstVar));
        }
    }
}
