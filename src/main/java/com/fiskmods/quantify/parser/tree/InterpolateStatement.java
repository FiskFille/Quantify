package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.jvm.VarAddress;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.MethodVisitor;

public record InterpolateStatement(Value progress, @Nullable VarAddress substitution, JvmFunction body) implements JvmFunction {
    @Override
    public void apply(final MethodVisitor mv) {
        if (progress instanceof NumLiteral(final double value) && value == 0) {
            return;
        }
        if (substitution != null) {
            substitution.set(mv, progress);
        }
        body.apply(mv);
    }
}
