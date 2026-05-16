package com.fiskmods.quantify.jvm.assignable;

import com.fiskmods.quantify.jvm.JvmTreeVisitor;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.parser.tree.Expression;
import com.fiskmods.quantify.parser.tree.NumLiteral;
import com.fiskmods.quantify.parser.tree.VarRef;

import java.util.List;

public record VarVisitorList(JvmTreeVisitor visitor, List<? extends VarRef> variables) implements VarVisitor {
    @Override
    public void visitGet() {
        for (final VarRef var : variables) {
            visitor.visitTree(var);
        }
    }

    @Override
    public void visitSet(final Expression value) {
        // Complex expressions only get calculated once for multi-var assignments
        if (variables.size() > 1 && !(value instanceof NumLiteral)) {
            Expression newValue = value;
            boolean first = true;

            for (final VarRef target : variables) {
                final Expression v = target.isNegated() ? Expression.negate(newValue) : newValue;
                visitor.varVisitor(target.address()).visitSet(v);

                // For all targets after the first, set them to the first target
                if (first) {
                    newValue = target.isNegated() ? Expression.negate(target) : target;
                    first = false;
                }
            }
            return;
        }

        for (final VarRef target : variables) {
            final Expression v = target.isNegated() ? Expression.negate(value) : value;
            visitor.varVisitor(target.address()).visitSet(v);
        }
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
