package com.fiskmods.quantify.jvm.assignable;

import com.fiskmods.quantify.jvm.JvmTreeVisitor;
import com.fiskmods.quantify.jvm.JvmUtil;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.library.QtfMath;
import com.fiskmods.quantify.parser.tree.Expression;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public record ArrayVarVisitor(JvmTreeVisitor visitor, MethodVisitor mv, int id, int arrayIndex) implements VarVisitor {
    @Override
    public void visitGet() {
        arrayAddress();
        mv.visitInsn(DALOAD);
    }

    @Override
    public void visitModify(final Expression value, final Operator operator) {
        arrayAddress();
        mv.visitInsn(DUP2);
        mv.visitInsn(DALOAD);
        visitor.visitExpression(value);
        operator.apply(mv);
        mv.visitInsn(DASTORE);
    }

    @Override
    public void visitSet(final Expression value) {
        arrayAddress();
        visitor.visitExpression(value);
        mv.visitInsn(DASTORE);
    }

    @Override
    public void visitInit() {
        arrayAddress();
        mv.visitInsn(DCONST_0);
        mv.visitInsn(DASTORE);
    }

    @Override
    public void visitLerp(final Expression value, final Expression progress, final boolean rotational) {
        arrayAddress();
        mv.visitInsn(DUP2);
        mv.visitInsn(DALOAD);
        visitor.visitExpression(progress);
        visitor.visitExpression(value);
        arrayAddress();
        mv.visitInsn(DALOAD);
        mv.visitInsn(DSUB);
        if (rotational) {
            QtfMath.WRAP_TO_PI.visit(mv, INVOKESTATIC, false);
        }
        mv.visitInsn(DMUL);
        mv.visitInsn(DADD);
        mv.visitInsn(DASTORE);
    }

    @Override
    public void visitLerpToZero(final Expression value, final Expression progress) {
        arrayAddress();
        mv.visitInsn(DUP2);
        mv.visitInsn(DALOAD);
        mv.visitInsn(DCONST_1);
        visitor.visitExpression(progress);
        mv.visitInsn(DSUB);
        mv.visitInsn(DMUL);
        mv.visitInsn(DASTORE);
    }

    private void arrayAddress() {
        mv.visitVarInsn(ALOAD, id);
        JvmUtil.iconst(mv, arrayIndex);
    }
}
