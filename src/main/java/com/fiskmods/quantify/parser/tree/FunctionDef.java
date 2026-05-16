package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.jvm.JvmFunctionDefinition;

public final class FunctionDef extends Statement implements JvmFunctionDefinition {
    private final String name;
    private final boolean isVisible;
    private final DefinedFunctionAddress address;
    private final Statement body;
    private final ReturnValueType returnValue;

    public FunctionDef(final String name, final boolean isVisible, final DefinedFunctionAddress address, final Statement body, final ReturnValueType returnValue) {
        this.name = name;
        this.isVisible = isVisible;
        this.address = address;
        this.body = body;
        this.returnValue = returnValue;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean isVisible() {
        return isVisible;
    }

    @Override
    public DefinedFunctionAddress address() {
        return address;
    }

    public Statement body() {
        return body;
    }

    public ReturnValueType returnValue() {
        return returnValue;
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

        @Override
        public String toString() {
            return getLoggingName();
        }
    }
}
