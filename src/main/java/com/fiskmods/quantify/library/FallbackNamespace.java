package com.fiskmods.quantify.library;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.VarInfo;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.member.Namespace;

public record FallbackNamespace(Namespace namespace, Namespace fallback) implements Namespace {
    @Override
    public <T extends VarAddress> T computeVariable(final VarType<T> type, final String name, final int modifiers) throws QtfException {
        if ((modifiers & VarInfo.DEFINITION) != 0 || namespace.hasVariable(name)) {
            return namespace.computeVariable(type, name, modifiers);
        }
        return fallback.computeVariable(type, name, modifiers);
    }

    @Override
    public boolean hasVariable(final String name) {
        return namespace.hasVariable(name) || fallback.hasVariable(name);
    }

    @Override
    public FunctionAddress getFunction(final String name) throws QtfException {
        if (namespace.hasFunction(name)) {
            return namespace.getFunction(name);
        }
        return fallback.getFunction(name);
    }

    @Override
    public boolean hasFunction(final String name) {
        return namespace.hasFunction(name) || fallback.hasFunction(name);
    }

    @Override
    public double getConstant(final String name) throws QtfException {
        if (namespace.hasConstant(name)) {
            return namespace.getConstant(name);
        }
        return fallback.getConstant(name);
    }

    @Override
    public boolean hasConstant(final String name) {
        return namespace.hasConstant(name) || fallback.hasConstant(name);
    }
}
