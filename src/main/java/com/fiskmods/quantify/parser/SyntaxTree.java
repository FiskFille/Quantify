package com.fiskmods.quantify.parser;

import com.fiskmods.quantify.jvm.JvmFunction;
import org.objectweb.asm.MethodVisitor;

import java.util.Collection;
import java.util.List;

public final class SyntaxTree implements JvmFunction {
    private final List<? extends JvmFunction> elements;

    public SyntaxTree(final List<? extends JvmFunction> elements) {
        this.elements = elements;
    }

    public static SyntaxTree of(final Collection<? extends JvmFunction> elements) {
        return new SyntaxTree(List.copyOf(elements));
    }

    @Override
    public void apply(final MethodVisitor mv) {
        for (final JvmFunction function : elements) {
            function.apply(mv);
        }
    }
}
