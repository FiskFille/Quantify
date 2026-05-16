package com.fiskmods.quantify.member;

import com.fiskmods.quantify.exception.QtfErrors;
import com.fiskmods.quantify.exception.QtfException;

import java.util.Map;
import java.util.Optional;

public class MemberMap implements Namespace {
    protected final Map<String, Member<?>> members;

    public MemberMap(final Map<String, Member<?>> members) {
        this.members = members;
    }

    @Override
    public Optional<Member<?>> find(final String name) {
        return Optional.ofNullable(members.get(name));
    }

    @Override
    public <T> Optional<T> find(final String name, final MemberType<T> expectedType) {
        return find(name).filter(expectedType)
                .map(member -> member.<T>uncheckedCast().value());
    }

    @Override
    public boolean has(final String name) {
        return members.containsKey(name);
    }

    @Override
    public boolean has(final String name, final MemberType<?> expectedType) {
        return find(name).filter(expectedType).isPresent();
    }

    @Override
    public <T> T get(final String name, final MemberType<T> expectedType) throws QtfException {
        final Member<?> foundMember = members.get(name);
        if (foundMember == null) {
            throw undefined(expectedType, name);
        }
        return foundMember.cast(expectedType).value();
    }

    protected QtfException undefined(final MemberType<?> expectedType, final String name) {
        return QtfErrors.undefined(expectedType, name);
    }

    public record Member<T>(String name, MemberType<T> type, T value) {
        public <R> Member<R> cast(final MemberType<R> expectedType) throws QtfException {
            if (type != expectedType) {
                throw new QtfException("Expected '%s' to be a %s, was %s".formatted(name, expectedType.name(), type.name()));
            }
            return uncheckedCast();
        }

        @SuppressWarnings("unchecked")
        private <R> Member<R> uncheckedCast() {
            return (Member<R>) this;
        }
    }
}
