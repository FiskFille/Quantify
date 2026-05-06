package com.fiskmods.quantify.jvm.assignable;

import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.library.QtfMath;
import com.fiskmods.quantify.parser.tree.NumLiteral;
import com.fiskmods.quantify.parser.tree.Value;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public record LocalVar(int id, boolean isNegated) implements VarAddress {

    public static LocalVar of(final int id) {
        return new LocalVar(id, false);
    }

    @Override
    public VarType<?> type() {
        return VarType.NUM;
    }

    @Override
    public LocalVar negate() {
        return new LocalVar(id, !isNegated);
    }

    @Override
    public void apply(final MethodVisitor mv) {
        mv.visitVarInsn(DLOAD, id);
        if (isNegated) {
            mv.visitInsn(DNEG);
        }
    }

    @Override
    public void modify(final MethodVisitor mv, final Value value, final Operator operator) {
        mv.visitVarInsn(DLOAD, id);
        value.apply(mv);
        operator.apply(mv);
        mv.visitVarInsn(DSTORE, id);
    }

    @Override
    public void set(final MethodVisitor mv, final Value value) {
        value.apply(mv);
        mv.visitVarInsn(DSTORE, id);
    }

    @Override
    public void lerp(final MethodVisitor mv, final Value value, final Value progress, final boolean rotational) {
        if (progress instanceof NumLiteral(final double v)) {
            if (v == 0) {
                return;
            }
            if (v == 1) {
                set(mv, value);
                return;
            }
        }

        // Interpolating towards 0 is the same as multiplying by (1-progress)
        if (!rotational && value instanceof NumLiteral(final double v) && v == 0) {
            mv.visitVarInsn(DLOAD, id);
            mv.visitInsn(DCONST_1);
            progress.apply(mv);
            mv.visitInsn(DSUB);
            mv.visitInsn(DMUL);
            mv.visitVarInsn(DSTORE, id);
            return;
        }

        mv.visitVarInsn(DLOAD, id);
        progress.apply(mv);
        value.apply(mv);
        mv.visitVarInsn(DLOAD, id);
        mv.visitInsn(DSUB);
        if (rotational) {
            QtfMath.WRAP_TO_PI.visit(mv, INVOKESTATIC, false);
        }
        mv.visitInsn(DMUL);
        mv.visitInsn(DADD);
        mv.visitVarInsn(DSTORE, id);
    }
}
