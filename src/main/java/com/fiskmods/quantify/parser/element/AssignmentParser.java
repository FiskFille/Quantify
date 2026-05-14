package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.*;

class AssignmentParser {
    public static SyntaxParser<Tree> parser(final Assignable target) {
        return (parser, context) -> {
            final Token assignment = parser.next(TokenClass.ASSIGNMENT);
            final Operator op = assignment.getAssignmentOperator(context, false);

            final Expression value = ExpressionParser.INSTANCE.accept(parser, context);
            if (op == Operator.LERP || op == Operator.LERP_ROT) {
                return new LerpAssignment(target, value, context.scope().getLerpProgress(), op == Operator.LERP_ROT);
            }
            return new Assignment(target, value, op);
        };
    }

    public static SyntaxParser<Tree> parserFrom(final String name, final Token.Range range, final Namespace namespace) {
        return (parser, context) -> {
            final VarAddress firstVar = VariableParser.compute(name, range, namespace, VarType.NUM, 0);

            if (parser.isNext(TokenClass.COMMA)) {
                final VariableList list = VariableParser.parseList(firstVar, 0).accept(parser, context);
                return parser(list).accept(parser, context);
            }
            return parser(firstVar).accept(parser, context);
        };
    }
}
