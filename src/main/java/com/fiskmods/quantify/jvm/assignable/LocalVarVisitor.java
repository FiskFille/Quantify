package com.fiskmods.quantify.jvm.assignable;

import com.fiskmods.quantify.jvm.JvmTreeVisitor;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.library.QtfMath;
import com.fiskmods.quantify.parser.tree.Value;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public record LocalVarVisitor(JvmTreeVisitor visitor, MethodVisitor mv, int id) implements VarVisitor {
    @Override
    public void visitGet() {
        mv.visitVarInsn(DLOAD, id);
    }

    @Override
    public void visitModify(final Value value, final Operator operator) {
        visitGet();
        visitor.visitTree(value);
        operator.apply(mv);
        mv.visitVarInsn(DSTORE, id);
    }

    @Override
    public void visitSet(final Value value) {
        visitor.visitTree(value);
        mv.visitVarInsn(DSTORE, id);
    }

    @Override
    public void visitInit() {
        mv.visitInsn(DCONST_0);
        mv.visitVarInsn(DSTORE, id);
    }

    @Override
    public void visitLerp(final Value value, final Value progress, final boolean rotational) {
        visitGet();
        visitor.visitTree(progress);
        visitor.visitTree(value);
        visitGet();
        mv.visitInsn(DSUB);
        if (rotational) {
            QtfMath.WRAP_TO_PI.visit(mv, INVOKESTATIC, false);
        }
        mv.visitInsn(DMUL);
        mv.visitInsn(DADD);
        mv.visitVarInsn(DSTORE, id);
    }

    @Override
    public void visitLerpToZero(final Value progress) {
        visitGet();
        mv.visitInsn(DCONST_1);
        visitor.visitTree(progress);
        mv.visitInsn(DSUB);
        mv.visitInsn(DMUL);
        mv.visitVarInsn(DSTORE, id);
    }
}
