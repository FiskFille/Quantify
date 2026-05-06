package com.fiskmods.quantify.parser.tree;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.DCONST_0;
import static org.objectweb.asm.Opcodes.DCONST_1;

public record NumLiteral(double value) implements Value {
    @Override
    public void apply(final MethodVisitor mv) {
        if (value == 0) {
            mv.visitInsn(DCONST_0);
        } else if (value == 1) {
            mv.visitInsn(DCONST_1);
        } else {
            mv.visitLdcInsn(value);
        }
    }

    @Override
    public boolean isNegated() {
        return value < 0;
    }

    @Override
    public Value negate() {
        if (value == 0 || Double.isNaN(value)) {
            return this;
        }
        return new NumLiteral(-value);
    }
}
