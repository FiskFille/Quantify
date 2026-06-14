package com.fiskmods.quantify.library;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.member.MemberMap;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.member.Namespace;

import java.util.Optional;

public record FallbackNamespace(Namespace namespace, Namespace fallback) implements Namespace {
    @Override
    public Optional<MemberMap.Member<?>> find(final String name) {
        return namespace.find(name).or(() -> fallback.find(name));
    }

    @Override
    public <T> Optional<T> find(final String name, final MemberType<T> expectedType) {
        return namespace.find(name, expectedType).or(() -> fallback.find(name, expectedType));
    }

    @Override
    public boolean has(final String name) {
        return namespace.has(name) || fallback.has(name);
    }

    @Override
    public boolean has(final String name, final MemberType<?> expectedType) {
        return namespace.has(name, expectedType) || fallback.has(name, expectedType);
    }

    @Override
    public <T> T get(final String name, final MemberType<T> expectedType) throws QtfException {
        try {
            return namespace.get(name, expectedType);
        } catch (final QtfException ignored) {
            return fallback.get(name, expectedType);
        }
    }

    @Override
    public <T extends VarAddress> T computeVariable(final VarType<T> type, final String name) throws QtfException {
        try {
            return namespace.computeVariable(type, name);
        } catch (final QtfException ignored) {
            return fallback.computeVariable(type, name);
        }
    }
}
