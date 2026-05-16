package com.fiskmods.quantify.member;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.library.FallbackNamespace;

import java.util.Optional;

public interface Namespace {
    Optional<MemberMap.Member<?>> find(String name);

    <T> Optional<T> find(String name, MemberType<T> expectedType);

    boolean has(String name);

    boolean has(String name, MemberType<?> expectedType);

    <T> T get(String name, MemberType<T> expectedType) throws QtfException;

    default <T extends VarAddress> T computeVariable(final VarType<T> type, final String name) throws QtfException {
        return get(name, MemberType.VARIABLE).cast(name, type);
    }

    default Namespace fallback(final Namespace fallbackNamespace) {
        if (fallbackNamespace == this) {
            return this;
        }
        return new FallbackNamespace(this, fallbackNamespace);
    }
}
