package com.fiskmods.quantify.jvm.assignable;

import com.fiskmods.quantify.jvm.JvmUtil;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.library.QtfMath;
import com.fiskmods.quantify.parser.element.Assignable;
import com.fiskmods.quantify.parser.element.NumLiteral;
import com.fiskmods.quantify.parser.element.Value;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public interface NumVar extends Value, Assignable {

    record Local(int id) implements NumVar {
        @Override
        public void apply(final MethodVisitor mv) {
            mv.visitVarInsn(DLOAD, id);
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

    record ArrayAccess(int id, int arrayIndex) implements NumVar {
        @Override
        public void apply(final MethodVisitor mv) {
            JvmUtil.arrayLoad(mv, id, arrayIndex);
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
}
