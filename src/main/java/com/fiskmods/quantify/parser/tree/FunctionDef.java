package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.jvm.JvmFunctionDefinition;

public record FunctionDef(
        String name,
        boolean isVisible,
        DefinedFunctionAddress address,
        Tree body,
        ReturnValueType returnValue
) implements Tree, JvmFunctionDefinition {

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

        @Override
        public String toString() {
            return getLoggingName();
        }
    }
}
