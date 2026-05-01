package com.fiskmods.quantify.member;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.NumVar;
import com.fiskmods.quantify.jvm.assignable.Struct;

import java.util.Optional;
import java.util.function.UnaryOperator;

public interface ScopeProvider {
    Scope scope();

    Scope global();

    void push(Scope scope);

    void pop();

    default void push(final UnaryOperator<Scope> scope) {
        push(scope.apply(scope()));
    }

    default Optional<MemberMap.Member<?>> findMember(final String name) {
        return scope().members.find(name);
    }

    default <T> Optional<T> findMember(final String name, final MemberType<T> expectedType) {
        return expectedType.scope(this).members.find(name, expectedType);
    }

    default boolean hasMember(final String name, final MemberType<?> expectedType) {
        return expectedType.scope(this).members.has(name, expectedType);
    }

    default <T> void addMember(final String name, final MemberType<T> type, final T value) throws QtfException {
        type.scope(this).members.put(name, type, value);
    }

    default VarAddress<NumVar> addLocalVariable(final String name) throws QtfException {
        return scope().addLocalVariable(name);
    }

    default VarAddress<Struct> addStruct(final String name) throws QtfException {
        return scope().addStruct(name);
    }

    default <T> T getMember(final String name, final MemberType<T> expectedType) throws QtfException {
        return expectedType.scope(this).members.get(name, expectedType);
    }
}
