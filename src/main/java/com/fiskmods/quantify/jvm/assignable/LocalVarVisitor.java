package com.fiskmods.quantify.jvm.assignable;

import com.fiskmods.quantify.jvm.JvmTreeVisitor;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.library.QtfMath;
import com.fiskmods.quantify.parser.tree.Expression;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public record LocalVarVisitor(JvmTreeVisitor visitor, MethodVisitor mv, int id) implements VarVisitor {
    @Override
    public void visitGet() {
        mv.visitVarInsn(DLOAD, id);
    }

    @Override
    public void visitModify(final Expression value, final Operator operator) {
        visitGet();
        visitor.visitExpression(value);
        operator.apply(mv);
        mv.visitVarInsn(DSTORE, id);
    }

    @Override
    public void visitSet(final Expression value, final boolean keepResult) {
        visitSet(() -> visitor.visitExpression(value), keepResult);
    }

    @Override
    public void visitSet(final Runnable value, final boolean keepResult) {
        value.run();
        if (keepResult) {
            mv.visitInsn(DUP2);
        }
        mv.visitVarInsn(DSTORE, id);
    }

    @Override
    public void visitInit() {
        mv.visitInsn(DCONST_0);
        mv.visitVarInsn(DSTORE, id);
    }

    @Override
    public void visitLerp(final Expression value, final VarAddress progress, final boolean rotational) {
        visitGet();
        visitor.varVisitor(progress).visitGet();
        visitor.visitExpression(value);
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
    public void visitLerpToZero(final Expression value, final VarAddress progress) {
        visitGet();
        mv.visitInsn(DCONST_1);
        visitor.varVisitor(progress).visitGet();
        mv.visitInsn(DSUB);
        mv.visitInsn(DMUL);
        mv.visitVarInsn(DSTORE, id);
    }
}
