package com.fiskmods.quantify.jvm;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.parser.element.Value;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public interface FunctionAddress {
    String owner();

    String name();

    String descriptor();

    int parameters();

    // TODO
    default String getLoggingName() {
        return owner() + "::" + name() + descriptor();
    }

    default void visit(final MethodVisitor mv, final int opcode, final boolean isInterface) {
        mv.visitMethodInsn(opcode, owner(), name(), descriptor(), isInterface);
    }

    default void run(final MethodVisitor mv, final Value[] args) {
        for (final Value arg : args) {
            arg.apply(mv);
        }
        visit(mv, Opcodes.INVOKESTATIC, false);
    }

    default void validateParameters(final int arguments, final Token.Range location) throws QtfParseException {
        if (arguments != parameters()) {
            throw new QtfParseException("Incorrect number of arguments for " + getLoggingName(), "expected %d, was %d".formatted(parameters(), arguments), location);
        }
    }

    static String descriptor(final int parameters) {
        return "(" + "D".repeat(parameters) + ")D";
    }

    static FunctionAddress create(final String owner, final String name, final int parameters) {
        return new Impl(owner, name, parameters);
    }

    record Impl(String owner, String name, String descriptor, int parameters) implements FunctionAddress {
        public Impl(final String owner, final String name, final int parameters) {
            this(owner, name, FunctionAddress.descriptor(parameters), parameters);
        }
    }
}
