package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.FunctionAddress;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class FunctionDef extends Statement {
    private final String name;
    private final List<ParameterTree> parameters;
    private final Statement body;
    private final ReturnValueType returnValue;

    public @Nullable FunctionAddress address;

    FunctionDef(final String name, final List<ParameterTree> parameters, final Statement body, final ReturnValueType returnValue) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
        this.returnValue = returnValue;
    }

    public String name() {
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

    public enum ReturnValueType {
        MISSING, IMPLICIT, EXPLICIT
    }
}
