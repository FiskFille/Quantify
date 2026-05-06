package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.lexer.token.Operator;
import org.objectweb.asm.MethodVisitor;

public interface Assignable extends JvmFunction {
    void set(MethodVisitor mv, Value value);

    default void init(final MethodVisitor mv) {
        set(mv, Value.ZERO);
    }

    void modify(MethodVisitor mv, Value value, Operator op);

    void lerp(MethodVisitor mv, Value value, Value progress, boolean rotational);
}
