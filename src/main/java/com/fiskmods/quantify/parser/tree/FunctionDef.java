package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.jvm.JvmClassComposer;
import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.jvm.JvmFunctionDefinition;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public record FunctionDef(String name, boolean isVisible, DefinedFunctionAddress address, JvmFunction body, ReturnValueType returnValue)
        implements JvmFunctionDefinition {

    @Override
    public JvmClassComposer define(final String className) {
        address.owner = className;
        return cw -> {
            final MethodVisitor mv = cw.visitMethod(ACC_STATIC | ACC_PUBLIC, address.name, address.descriptor, null, null);
            body.apply(mv);

            if (returnValue == ReturnValueType.MISSING) {
                mv.visitInsn(DCONST_0);
                mv.visitInsn(DRETURN);
            } else if (returnValue == ReturnValueType.IMPLICIT) {
                mv.visitInsn(DRETURN);
            }
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        };
    }

    public enum ReturnValueType {
        MISSING, IMPLICIT, EXPLICIT
    }

    public static class DefinedFunctionAddress implements FunctionAddress {
        public String owner;
        public String name;
        public String descriptor;
        public int parameters;

        @Override
        public String owner() {
            return owner;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String descriptor() {
            return descriptor;
        }

        @Override
        public int parameters() {
            return parameters;
        }
    }
}
