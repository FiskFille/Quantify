package com.fiskmods.quantify.jvm.assignable;

import com.fiskmods.quantify.jvm.JvmUtil;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.parser.tree.Expression;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public record StructVarVisitor(MethodVisitor mv, int index, int size) implements VarVisitor {
    @Override
    public void visitGet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitSet(final Expression value, final boolean keepResult) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitSet(final Runnable value, final boolean keepResult) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitInit() {
        JvmUtil.iconst(mv, size);
        mv.visitIntInsn(NEWARRAY, T_DOUBLE);
        mv.visitVarInsn(ASTORE, index);
    }

    @Override
    public void visitModify(final Expression value, final Operator op) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitLerp(final Expression value, final VarAddress progress, final boolean rotational) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitLerpToZero(final Expression value, final VarAddress progress) {
        throw new UnsupportedOperationException();
    }
}
