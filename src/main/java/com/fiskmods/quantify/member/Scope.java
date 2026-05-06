package com.fiskmods.quantify.member;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.LocalVar;
import com.fiskmods.quantify.jvm.assignable.Struct;
import com.fiskmods.quantify.parser.tree.Value;

import java.util.function.IntFunction;
import java.util.function.Supplier;

public class Scope {
    public final MemberMap members = new MemberMap();
    protected final int level;

    protected Namespace namespace;
    protected Value lerpProgress;

    protected int localIndexOffset = 3;

    public Scope(final Namespace namespace, final int level) {
        this.namespace = namespace;
        this.level = level;
    }

    public Scope copy(final Namespace namespace) {
        final Scope scope = new Scope(namespace, level + 1);
        scope.lerpProgress = lerpProgress;
        scope.localIndexOffset = localIndexOffset;
        scope.members.inherit(members);
        return scope;
    }

    public Scope copy() {
        return copy(namespace);
    }

    public void setNamespace(final Namespace namespace) {
        this.namespace = namespace;
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public void setLerpProgress(final Value lerpProgress) {
        this.lerpProgress = lerpProgress;
    }

    public Value getLerpProgress() {
        return lerpProgress;
    }

    public boolean isInnerScope() {
        return level > 0;
    }

    public <T extends VarAddress> T addLocalVariable(final String name, final IntFunction<T> supplier) throws QtfException {
        return members.putVariable(name, (Supplier<T>) () -> {
            final T var = supplier.apply(localIndexOffset);
            localIndexOffset += var.type().size();
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
