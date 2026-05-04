package com.fiskmods.quantify.member;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.library.FallbackNamespace;

public interface Namespace {
    <T extends VarAddress> T computeVariable(VarType<T> type, String name, int modifiers) throws QtfException;

    boolean hasVariable(String name);

    FunctionAddress getFunction(String name) throws QtfException;

    boolean hasFunction(String name);

    double getConstant(String name) throws QtfException;

    boolean hasConstant(String name);

    default Namespace fallback(final Namespace fallbackNamespace) {
        if (fallbackNamespace == this) {
            return this;
        }
        return new FallbackNamespace(this, fallbackNamespace);
    }
}
