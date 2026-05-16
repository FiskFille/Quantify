package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.tree.*;

import java.util.List;

class AssignmentParser {
    private static Statement parseAssignment(final QtfParser parser, final SyntaxContext context, final List<? extends VarRef> targets) throws QtfParseException {
        final Token assignment = parser.next(TokenClass.ASSIGNMENT);
        final Operator op = assignment.getAssignmentOperator(context, false);

        final Expression value = ExpressionParser.INSTANCE.accept(parser, context);
        if (op == Operator.LERP || op == Operator.LERP_ROT) {
            return new LerpAssignment(targets, value, context.scope().getLerpProgress(), op == Operator.LERP_ROT);
        } else {
            return new Assignment(targets, value, op);
        }
    }

    static Statement parseAssignment(final QtfParser parser, final SyntaxContext context, final String name, final Token.Range range, final Namespace namespace) throws QtfParseException {
        final VarRef firstVar = VariableParser.compute(name, range, namespace, VarType.NUM, false);

        if (parser.isNext(TokenClass.COMMA)) {
            final List<? extends VarRef> list = VariableParser.parseList(parser, context, firstVar);
            return parseAssignment(parser, context, list);
        } else {
            return parseAssignment(parser, context, List.of(firstVar));
        }
    }
}
