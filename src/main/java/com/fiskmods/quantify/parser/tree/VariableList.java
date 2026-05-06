package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.JvmUtil;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.lexer.token.Operator;
import org.objectweb.asm.MethodVisitor;

public record VariableList<T extends VarAddress>(T[] addresses) implements Assignable {
    @Override
    public void apply(final MethodVisitor mv) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(final MethodVisitor mv, final Value value) {
        JvmUtil.set(mv, addresses, value);
    }

    @Override
    public void init(final MethodVisitor mv) {
        for (final VarAddress address : addresses) {
            address.init(mv);
        }
    }

    @Override
    public void modify(final MethodVisitor mv, final Value value, final Operator op) {
        JvmUtil.modify(mv, addresses, value, op);
    }

    @Override
    public void lerp(final MethodVisitor mv, final Value value, final Value progress, final boolean rotational) {
        JvmUtil.lerp(mv, addresses, progress, rotational, value);
    }
}
