package com.fiskmods.quantify.member;

import com.fiskmods.quantify.exception.QtfErrors;
import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.VarAddress;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class MemberMap {
    private final Map<String, Member<?>> members = new HashMap<>();

    public void inherit(final MemberMap other) {
        members.putAll(other.members);
    }

    public void inheritAllExcept(final MemberMap other, final MemberType<?> exceptType) {
        for (final Map.Entry<String, Member<?>> e : other.members.entrySet()) {
            if (e.getValue().type() == exceptType) {
                continue;
            }
            members.put(e.getKey(), e.getValue());
        }
    }

    public void forEach(final BiConsumer<String, Member<?>> action) {
        members.forEach(action);
    }

    public Optional<Member<?>> find(final String name) {
        return Optional.ofNullable(members.get(name));
    }

    public boolean has(final String name) {
        return find(name).isPresent();
    }

    public <T> Optional<T> find(final String name, final MemberType<T> expectedType) {
        return find(name).filter(expectedType)
                .map(member -> member.<T>uncheckedCast().value);
    }

    public boolean has(final String name, final MemberType<?> expectedType) {
        return find(name).filter(expectedType).isPresent();
    }

    public void nameCheck(final String name) throws QtfException {
        if (members.containsKey(name)) {
            throw new QtfException("Duplicate member '%s'".formatted(name));
        }
    }

    public <T> void put(final String name, final MemberType<T> type, final T value) throws QtfException {
        nameCheck(name);
        members.put(name, new Member<>(type, value));
    }

    public <T extends VarAddress> T putVariable(final String name, final Function<String, T> address) throws QtfException {
        nameCheck(name);
        final T value = address.apply(name);
        members.put(name, new Member<>(MemberType.VARIABLE, value));
        return value;
    }

    public <T extends VarAddress> T putVariable(final String name, final Supplier<T> address) throws QtfException {
        return this.<T>putVariable(name, ignored -> address.get());
    }

    public <T extends VarAddress> T putVariable(final String name, final T address) throws QtfException {
        return this.<T>putVariable(name, () -> address);
    }

    public <T> T get(final String name, final MemberType<T> expectedType) throws QtfException {
        final Member<?> foundMember = members.get(name);
        if (foundMember == null) {
            throw QtfErrors.undefined(expectedType, name);
        }
        return foundMember.cast(name, expectedType).value;
    }

    public record Member<T>(MemberType<T> type, T value) {
        public <R> Member<R> cast(final String name, final MemberType<R> expectedType) throws QtfException {
            if (type != expectedType) {
                throw new QtfException("Expected '%s' to be a %s, was %s".formatted(name, expectedType.name(), type.name()));
            }
            return uncheckedCast();
        }

        @SuppressWarnings("unchecked")
        private  <R> Member<R> uncheckedCast() {
            return (Member<R>) this;
        }
    }
}
