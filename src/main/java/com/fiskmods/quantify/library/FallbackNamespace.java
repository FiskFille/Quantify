package com.fiskmods.quantify.library;

import com.fiskmods.quantify.exception.QtfException;
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
        return namespace.has(name, expectedType)
                ? namespace.get(name, expectedType) : fallback.get(name, expectedType);
    }
}
