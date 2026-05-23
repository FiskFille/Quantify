package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.FunctionAddress;

public final class FunctionDef extends Statement {
    private final Identifier name;
    private final DefinedFunctionAddress address;
    private final Statement body;
    private final ReturnValueType returnValue;

    FunctionDef(final Identifier name, final DefinedFunctionAddress address, final Statement body, final ReturnValueType returnValue) {
        this.name = name;
        this.address = address;
        this.body = body;
        this.returnValue = returnValue;
    }

    public Identifier name() {
        return name;
    }

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
