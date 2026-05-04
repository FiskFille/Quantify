package com.fiskmods.quantify.jvm.assignable;

import com.fiskmods.quantify.jvm.JvmUtil;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.library.QtfMath;
import com.fiskmods.quantify.parser.element.NumLiteral;
import com.fiskmods.quantify.parser.element.Value;
import org.objectweb.asm.MethodVisitor;

import java.util.function.Function;
import java.util.function.ToIntFunction;

import static org.objectweb.asm.Opcodes.*;

public record ArrayVar(int id, int arrayIndex, boolean isNegated) implements VarAddress {

    public static ArrayVar of(final int id, final int arrayIndex) {
        return new ArrayVar(id, arrayIndex, false);
    }

    public static <T> Function<T, ArrayVar> of(final int id, final ToIntFunction<T> arrayIndex) {
        return t -> of(id, arrayIndex.applyAsInt(t));
    }

    @Override
    public VarType<?> type() {
        return VarType.NUM;
    }

    @Override
    public ArrayVar negate() {
        return new ArrayVar(id, arrayIndex, !isNegated);
    }

    @Override
    public void apply(final MethodVisitor mv) {
        JvmUtil.arrayLoad(mv, id, arrayIndex);
        if (isNegated) {
            mv.visitInsn(DNEG);
        }
    }

    @Override
    public void modify(final MethodVisitor mv, final Value value, final Operator operator) {
        JvmUtil.arrayModify(mv, id, arrayIndex, value.andThen(operator));
    }

    @Override
    public void set(final MethodVisitor mv, final Value value) {
        JvmUtil.arrayStore(mv, id, arrayIndex, value);
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
            JvmUtil.arrayModify(mv, id, arrayIndex, ignored -> {
                mv.visitInsn(DCONST_1);
                progress.apply(mv);
                mv.visitInsn(DSUB);
                mv.visitInsn(DMUL);
            });
            return;
        }

        JvmUtil.arrayModify(mv, id, arrayIndex, ignored -> {
            progress.apply(mv);
            value.apply(mv);
            JvmUtil.arrayLoad(mv, id, arrayIndex);
            mv.visitInsn(DSUB);
            if (rotational) {
                QtfMath.WRAP_TO_PI.visit(mv, INVOKESTATIC, false);
            }
            mv.visitInsn(DMUL);
            mv.visitInsn(DADD);
        });
    }
}
