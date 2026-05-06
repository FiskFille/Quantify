package com.fiskmods.quantify.parser.tree;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public record Return(Value value) implements Value {
    @Override
    public void apply(final MethodVisitor mv) {
        value.apply(mv);
        mv.visitInsn(Opcodes.DRETURN);
    }
}
