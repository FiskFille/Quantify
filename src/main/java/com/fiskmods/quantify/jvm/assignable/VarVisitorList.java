package com.fiskmods.quantify.jvm.assignable;

import com.fiskmods.quantify.jvm.JvmTreeVisitor;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.parser.tree.Expression;
import com.fiskmods.quantify.parser.tree.NumLiteral;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.DNEG;

public record VarVisitorList(JvmTreeVisitor visitor, MethodVisitor mv, VarVisitor[] variables, boolean[] isNegated) implements VarVisitor {

    public static VarVisitorList of(final JvmTreeVisitor visitor, final MethodVisitor mv, final VarAddress[] addresses) {
        final VarVisitor[] variables = new VarVisitor[addresses.length];
        for (int i = 0; i < variables.length; i++) {
            variables[i] = visitor.varVisitor(addresses[i]);
        }
        return new VarVisitorList(visitor, mv, variables, new boolean[variables.length]);
    }

    @Override
    public void visitGet() {
        for (int i = 0; i < variables.length; i++) {
            variables[i].visitGet();
            if (isNegated[i]) {
                mv.visitInsn(DNEG);
            }
        }
    }

    @Override
    public void visitSet(final Expression value, final boolean keepResult) {
        // Complex expressions only get calculated once for multi-var assignments
        if (variables.length > 1 && !(value instanceof NumLiteral)) {
            final int lastIndex = variables.length - 1;
            visitComplexSet(value, keepResult, lastIndex);

            if (keepResult && isNegated[lastIndex]) {
                mv.visitInsn(DNEG);
            }
            return;
        }

        for (int i = 0; i < variables.length; i++) {
            final Expression v = isNegated[i] ? Expression.negate(value) : value;
            variables[i].visitSet(v, false);
        }
        if (keepResult) {
            visitor.visitExpression(value);
        }
    }

    private void visitComplexSet(final Expression value, final boolean keepResult, final int index) {
        if (index > 0) {
            variables[index].visitSet(() -> {
                        visitComplexSet(value, true, index - 1);
                        if (isNegated[index] != isNegated[index - 1]) {
                            mv.visitInsn(DNEG);
                        }
                    },
                    keepResult
            );
        } else {
            variables[0].visitSet(
                    () -> visitor.visitExpression(isNegated[0] ? Expression.negate(value) : value),
                    keepResult
            );
        }
    }

    @Override
    public void visitSet(final Runnable value, final boolean keepResult) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitInit() {
        for (final VarVisitor target : variables) {
            target.visitInit();
        }
    }

    @Override
    public void visitModify(final Expression value, final Operator op) {
        for (int i = 0; i < variables.length; i++) {
            final Expression v = isNegated[i] ? Expression.negate(value) : value;
            variables[i].visitModify(v, op);
        }
    }

    @Override
    public void visitLerp(final Expression value, final VarAddress progress, final boolean rotational) {
        for (int i = 0; i < variables.length; i++) {
            final Expression v = isNegated[i] ? Expression.negate(value) : value;
            variables[i].visitLerp(v, progress, rotational);
        }
    }

    @Override
    public void visitLerpToZero(final Expression value, final VarAddress progress) {
        visitLerp(value, progress, false);
    }
}
