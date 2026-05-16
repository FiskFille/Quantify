package com.fiskmods.quantify.member;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.VarAddress;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class MutableMemberMap extends MemberMap {
    public MutableMemberMap() {
        super(new HashMap<>());
    }

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

    private void nameCheck(final String name) throws QtfException {
        if (members.containsKey(name)) {
            throw new QtfException("Duplicate member '%s'".formatted(name));
        }
    }

    public <T> void put(final String name, final MemberType<T> type, final T value) throws QtfException {
        nameCheck(name);
        members.put(name, new Member<>(name, type, value));
    }

    public <T extends VarAddress> T putVariable(final String name, final Function<String, T> address) throws QtfException {
        nameCheck(name);
        final T value = address.apply(name);
        members.put(name, new Member<>(name, MemberType.VARIABLE, value));
        return value;
    }

    public <T extends VarAddress> T putVariable(final String name, final Supplier<T> address) throws QtfException {
        return this.<T>putVariable(name, ignored -> address.get());
    }

    public <T extends VarAddress> T putVariable(final String name, final T address) throws QtfException {
        return this.<T>putVariable(name, () -> address);
    }
}
