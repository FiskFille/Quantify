package com.fiskmods.quantify.jvm;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

@FunctionalInterface
public interface JvmFunction {
    JvmFunction _ADD = insn(DADD);
    JvmFunction _SUB = insn(DSUB);
    JvmFunction _MUL = insn(DMUL);
    JvmFunction _DIV = insn(DDIV);
    JvmFunction _REM = insn(DREM);
    JvmFunction _POW = mv -> mv.visitMethodInsn(INVOKESTATIC,
            "java/lang/Math",
            "pow", "(DD)D", false);

    JvmFunction _EQS = comparator(IFEQ);
    JvmFunction _NEQ = comparator(IFNE);
    JvmFunction _LT = comparator(IFLT);
    JvmFunction _LEQ = comparator(IFLE);
    JvmFunction _GT = comparator(IFGT);
    JvmFunction _GEQ = comparator(IFGE);

    void apply(MethodVisitor mv);

    static JvmFunction insn(final int opcode) {
        return mv -> mv.visitInsn(opcode);
    }

    static JvmFunction comparator(final int opcode) {
        return mv -> {
            final Label l = new Label();
            final Label end = new Label();
            mv.visitInsn(DCMPG);
            mv.visitJumpInsn(opcode, l);
            mv.visitInsn(DCONST_0);
            mv.visitJumpInsn(GOTO, end);
            mv.visitLabel(l);
            mv.visitInsn(DCONST_1);
            mv.visitLabel(end);
        };
    }
}
