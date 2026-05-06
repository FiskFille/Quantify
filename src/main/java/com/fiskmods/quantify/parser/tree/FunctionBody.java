package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.parser.SyntaxTree;
import org.objectweb.asm.MethodVisitor;

public record FunctionBody(SyntaxTree tree) implements JvmFunction {
    @Override
    public void apply(final MethodVisitor mv) {
        tree.apply(mv);
    }
}
