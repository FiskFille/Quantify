package com.fiskmods.quantify.member;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.NumVar;
import com.fiskmods.quantify.jvm.assignable.Struct;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.parser.element.Assignable;
import com.fiskmods.quantify.parser.element.Value;

import java.util.function.IntFunction;

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

    public <T extends Value & Assignable> VarAddress<T> addLocalVariable(final String name, final VarType<T> type, final IntFunction<VarAddress<T>> supplier) throws QtfException {
        return members.putVariable(name, () -> {
            final VarAddress<T> var = supplier.apply(localIndexOffset);
            localIndexOffset += type.size();
            return var;
        });
    }

    public VarAddress<NumVar> addLocalVariable(final String name) throws QtfException {
        return addLocalVariable(name, VarType.NUM, VarAddress::local);
    }

    public VarAddress<Struct> addStruct(final String name) throws QtfException {
        return addLocalVariable(name, VarType.STRUCT, Struct::create);
    }
}
