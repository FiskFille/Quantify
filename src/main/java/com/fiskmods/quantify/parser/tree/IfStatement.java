package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.JvmFunction;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public record IfStatement(Value condition, JvmFunction body, @Nullable JvmFunction elseBody) implements JvmFunction {
    @Override
    public void apply(final MethodVisitor mv) {
        if (condition instanceof NumLiteral(final double value)) {
            if (value > 0) {
                body.apply(mv);
            }
            return;
        }

        final Label end = new Label();
        condition.apply(mv);
        mv.visitInsn(D2I);

        if (elseBody == null) {
            mv.visitJumpInsn(IFLE, end);
            body.apply(mv);
        } else {
            final Label els = new Label();
            mv.visitJumpInsn(IFLE, els);
            body.apply(mv);
            mv.visitJumpInsn(GOTO, end);
            mv.visitLabel(els);
            elseBody.apply(mv);
        }
        mv.visitLabel(end);
    }
}
