package com.fiskmods.quantify.jvm.assignable;

import com.fiskmods.quantify.jvm.JvmTreeVisitor;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.parser.tree.NumLiteral;
import com.fiskmods.quantify.parser.tree.Value;

import java.util.List;

public record VarVisitorList(JvmTreeVisitor visitor, List<? extends VarAddress> addresses) implements VarVisitor {
    @Override
    public void visitGet() {
        for (final VarAddress var : addresses) {
            visitor.visitTree(var);
        }
    }

    @Override
    public void visitSet(final Value value) {
        // Complex expressions only get calculated once for multi-var assignments
        if (addresses.size() > 1 && !(value instanceof NumLiteral)) {
            Value newValue = value;
            boolean first = true;

            for (final VarAddress target : addresses) {
                final Value v = newValue.negateIf(target.isNegated());
                visitor.varVisitor(target).visitSet(v);

                // For all targets after the first, set them to the first target
                if (first) {
                    newValue = target.negateIf(target.isNegated());
                    first = false;
                }
            }
            return;
        }

        for (final VarAddress target : addresses) {
            final Value v = value.negateIf(target.isNegated());
            visitor.varVisitor(target).visitSet(v);
        }
    }

    @Override
    public void visitInit() {
        for (final VarAddress target : addresses) {
            visitor.varVisitor(target).visitInit();
        }
    }

    @Override
    public void visitModify(final Value value, final Operator op) {
        for (final VarAddress target : addresses) {
            final Value v = value.negateIf(target.isNegated());
            visitor.varVisitor(target).visitModify(v, op);
        }
    }

    @Override
    public void visitLerp(final Value value, final Value progress, final boolean rotational) {
        for (final VarAddress target : addresses) {
            final Value v = value.negateIf(target.isNegated());
            visitor.varVisitor(target).visitLerp(v, progress, rotational);
        }
    }

    @Override
    public void visitLerpToZero(final Value progress) {
        visitLerp(new NumLiteral(0), progress, false);
    }
}
