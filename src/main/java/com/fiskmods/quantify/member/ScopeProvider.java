package com.fiskmods.quantify.member;

import com.fiskmods.quantify.exception.QtfException;

import java.util.function.UnaryOperator;

public interface ScopeProvider {
    Scope scope();

    Scope global();

    void push(Scope scope);

    void pop();

    int stackDepth();

    default void push(final UnaryOperator<Scope> scope) {
        push(scope.apply(scope()));
    }

    default <T> void addMember(final String name, final MemberType<T> type, final T value) throws QtfException {
        type.scope(this).members.put(name, type, value);
    }

    default <T> T getMember(final String name, final MemberType<T> expectedType) throws QtfException {
        return expectedType.scope(this).members.get(name, expectedType);
    }
}
