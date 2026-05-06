package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.FunctionAddress;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public record FunctionRef(FunctionAddress address, Value[] args, boolean hasResult) implements Value {
    static FunctionRef call(final FunctionAddress address, final Value... args) {
        return new FunctionRef(address, args, true);
    }

    static FunctionRef run(final FunctionAddress address, final Value... args) {
        return new FunctionRef(address, args, false);
    }

    @Override
    public void apply(final MethodVisitor mv) {
        address.run(mv, args);

        // Pop returned function value from stack if unused
        if (!hasResult) {
            mv.visitInsn(Opcodes.POP2);
        }
    }
}
