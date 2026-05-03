package com.fiskmods.quantify.member;

import com.fiskmods.quantify.jvm.JvmRunnable;
import com.fiskmods.quantify.util.IndexMap;

public class Variable implements VarReference {
    private VarReference reference = VarReference.EMPTY;

    private Variable() {
    }

    public static Variable create() {
        return new Variable();
    }

    @Override
    public double get() {
        return reference.get();
    }

    @Override
    public void set(final double value) {
        reference.set(value);
    }

    @Override
    public boolean isEmpty() {
        return reference.isEmpty();
    }

    public static QtfListener.Resolver resolve(final IndexMap<String> outputs, final JvmRunnable runnable) {
        return (var, name) -> var.reference = runnable.resolve(outputs.get(name));
    }
}
