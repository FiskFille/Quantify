package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.FunctionAddress;

import java.util.List;

public final class FunctionDef extends Statement {
    private final Identifier name;
    private final List<ParameterTree> parameters;
    private final Statement body;
    private final ReturnValueType returnValue;

    private final DefinedFunctionAddress address;

    FunctionDef(final Identifier name, final List<ParameterTree> parameters, final Statement body, final ReturnValueType returnValue, final DefinedFunctionAddress address) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
        this.returnValue = returnValue;
        this.address = address;
    }

    public Identifier name() {
        return name;
    }

    public List<ParameterTree> parameters() {
        return parameters;
    }

    public Statement body() {
        return body;
    }

    public ReturnValueType returnValue() {
        return returnValue;
    }

    public DefinedFunctionAddress address() {
        return address;
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
