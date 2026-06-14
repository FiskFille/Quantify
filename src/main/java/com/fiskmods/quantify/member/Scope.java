package com.fiskmods.quantify.member;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.LocalVar;
import com.fiskmods.quantify.jvm.assignable.Struct;

import java.util.function.IntFunction;
import java.util.function.Supplier;

public class Scope {
    public final MutableMemberMap members = new MutableMemberMap();
    protected final int level;

    protected int localIndexOffset = 3;

    public Scope(final int level) {
        this.level = level;
    }

    public Scope copy() {
        final Scope scope = new Scope(level + 1);
        scope.localIndexOffset = localIndexOffset;
        scope.members.inherit(members);
        return scope;
    }

    public boolean isInnerScope() {
        return level > 0;
    }

    public <T extends VarAddress> T addLocalVariable(final String name, final IntFunction<T> supplier) throws QtfException {
        return members.putVariable(name, (Supplier<T>) () -> {
            final T var = supplier.apply(localIndexOffset);
            localIndexOffset += var.type().internal().getSize();
            return var;
        });
    }

    public LocalVar addLocalVariable(final String name) throws QtfException {
        return addLocalVariable(name, LocalVar::of);
    }

    public Struct addStruct(final String name) throws QtfException {
        return addLocalVariable(name, Struct::of);
    }
}
