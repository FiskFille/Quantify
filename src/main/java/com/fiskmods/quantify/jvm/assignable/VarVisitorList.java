package com.fiskmods.quantify.jvm.assignable;

import com.fiskmods.quantify.jvm.JvmTreeVisitor;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.parser.tree.Expression;
import com.fiskmods.quantify.parser.tree.NumLiteral;
import com.fiskmods.quantify.parser.tree.VarRef;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

import static org.objectweb.asm.Opcodes.DNEG;

public record VarVisitorList(JvmTreeVisitor visitor, MethodVisitor mv, List<? extends VarRef> variables) implements VarVisitor {
    @Override
    public void visitGet() {
        for (final VarRef var : variables) {
            visitor.visitExpression(var);
        }
    }

    @Override
    public void visitSet(final Expression value, final boolean keepResult) {
        // Complex expressions only get calculated once for multi-var assignments
        if (variables.size() > 1 && !(value instanceof NumLiteral)) {
            visitComplexSet(value, keepResult, variables().size() - 1);
            if (keepResult && variables.getLast().isNegated()) {
                mv.visitInsn(DNEG);
            }
            return;
        }

        for (final VarRef target : variables) {
            final Expression v = target.isNegated() ? Expression.negate(value) : value;
            visitor.varVisitor(target.address()).visitSet(v, false);
        }
        if (keepResult) {
            visitor.visitExpression(value);
        }
    }

    private VarRef visitComplexSet(final Expression value, final boolean keepResult, final int index) {
        final VarRef target = variables.get(index);
        if (index > 0) {
            visitor.varVisitor(target.address()).visitSet(() -> {
                        final VarRef preceding = visitComplexSet(value, true, index - 1);
                        if (target.isNegated() != preceding.isNegated()) {
                            mv.visitInsn(DNEG);
                        }
                    },
                    keepResult
            );
        } else {
            visitor.varVisitor(target.address()).visitSet(
                    () -> visitor.visitExpression(target.isNegated() ? Expression.negate(value) : value),
                    keepResult
            );
        }
        return target;
    }

    @Override
    public void visitSet(final Runnable value, final boolean keepResult) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitInit() {
        for (final VarRef target : variables) {
            visitor.varVisitor(target.address()).visitInit();
        }
    }

    @Override
    public void visitModify(final Expression value, final Operator op) {
        for (final VarRef target : variables) {
            final Expression v = target.isNegated() ? Expression.negate(value) : value;
            visitor.varVisitor(target.address()).visitModify(v, op);
        }
    }

    @Override
    public void visitLerp(final Expression value, final Expression progress, final boolean rotational) {
        for (final VarRef target : variables) {
            final Expression v = target.isNegated() ? Expression.negate(value) : value;
            visitor.varVisitor(target.address()).visitLerp(v, progress, rotational);
        }
    }

    @Override
    public void visitLerpToZero(final Expression value, final Expression progress) {
        visitLerp(value, progress, false);
    }
}
