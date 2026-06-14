package com.fiskmods.quantify.jvm;

import com.fiskmods.quantify.exception.QtfException;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.MethodVisitor;

import java.util.Objects;

public record FunctionAddress(
        @Nullable String owner,
        String name,
        String descriptor,
        int parameters
) {
    public FunctionAddress(final @Nullable String owner, final String name, final int parameters) {
        this(owner, name, FunctionAddress.descriptor(parameters), parameters);
    }

    public FunctionAddress withOwner(final String owner) {
        return new FunctionAddress(owner, name, descriptor, parameters);
    }

    // TODO
    public String getLoggingName() {
        return owner + "::" + name + descriptor;
    }

    public void visit(final MethodVisitor mv, final int opcode, final boolean isInterface, final String ownerClass) {
        mv.visitMethodInsn(opcode, owner != null ? owner : ownerClass, name, descriptor, isInterface);
    }

    public void visit(final MethodVisitor mv, final int opcode, final boolean isInterface) {
        mv.visitMethodInsn(opcode, Objects.requireNonNull(owner, "owner"), name, descriptor, isInterface);
    }

    public void validateParameters(final int arguments) throws QtfException {
        if (arguments != parameters) {
            throw new QtfException("Incorrect number of arguments for " + getLoggingName() + " - expected %d, was %d".formatted(parameters, arguments));
        }
    }

    public static String descriptor(final int parameters) {
        return "(" + "D".repeat(parameters) + ")D";
    }

    public static FunctionAddress create(final @Nullable String owner, final String name, final int parameters) {
        return new FunctionAddress(owner, name, parameters);
    }
}
