package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxParser;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.MethodVisitor;

interface Assignment extends JvmFunction {
    static SyntaxParser<Assignment> parser(final Assignable target, final boolean isDefinition) {
        return (parser, context) -> {
            // Empty definition
            if (!parser.hasNext(QtfParser.Boundary.LINE)) {
                return new AbsoluteAssignment(target, null, null);
            }

            final Token assignment = parser.next(TokenClass.ASSIGNMENT);
            final Operator op = assignment.getAssignmentOperator(context, isDefinition);

            final Value value = parser.next(ExpressionParser.INSTANCE);
            if (op == Operator.LERP || op == Operator.LERP_ROT) {
                return new LerpAssignment(target, value, context.scope().getLerpProgress(), op == Operator.LERP_ROT);
            }
            return new AbsoluteAssignment(target, value, op);
        };
    }

    static SyntaxParser<Assignment> parserFrom(final String name, final Token.Range range, final Namespace namespace) {
        return (parser, context) -> {
            final VarAddress firstVar = VariableParser.compute(name, range, namespace, VarType.NUM, 0);

            if (parser.isNext(TokenClass.COMMA)) {
                final VariableList<?> list = parser.next(VariableList.parse(firstVar, 0));
                return parser.next(parser(list, false));
            }
            return parser.next(parser(firstVar, false));
        };
    }

    record AbsoluteAssignment(Assignable target, @Nullable Value value, @Nullable Operator op) implements Assignment {
        @Override
        public void apply(final MethodVisitor mv) {
            if (value == null) {
                target.init(mv);
            } else if (op != null) {
                target.modify(mv, value, op);
            } else {
                target.set(mv, value);
            }
        }
    }

    record LerpAssignment(Assignable target, Value value, Value progress, boolean rotational) implements Assignment {
        @Override
        public void apply(final MethodVisitor mv) {
            target.lerp(mv, value, progress, rotational);
        }
    }
}
