package com.fiskmods.quantify.member;

import com.fiskmods.quantify.exception.QtfErrors;
import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.FunctionAddress;

import java.util.HashMap;
import java.util.Map;

public final class SimpleNamespace extends MemberMap {
    private final String namespaceName;

    public SimpleNamespace(final String namespaceName, final Map<String, MemberMap.Member<?>> members) {
        super(Map.copyOf(members));
        this.namespaceName = namespaceName;
    }

    @Override
    protected QtfException undefined(final MemberType<?> expectedType, final String name) {
        return QtfErrors.undefined(expectedType, name, namespaceName);
    }

    public static Builder builder(final String name) {
        return new Builder(name);
    }

    public static final class Builder {
        private final String namespaceName;
        private final Map<String, MemberMap.Member<?>> members = new HashMap<>();

        private Builder(final String namespaceName) {
            this.namespaceName = namespaceName;
        }

        public Builder addConstant(final String name, final double value) {
            members.put(name, new MemberMap.Member<>(name, MemberType.CONSTANT, value));
            return this;
        }

        public Builder addFunction(final String name, final FunctionAddress function) {
            members.put(name, new MemberMap.Member<>(name, MemberType.FUNCTION, function));
            return this;
        }

        public Builder addFunction(final FunctionAddress function) {
            return addFunction(function.name(), function);
        }

        public Builder addFunction(final String owner, final String name, final int parameters) {
            return addFunction(name, FunctionAddress.create(owner, name, parameters));
        }

        public Namespace build() {
            return new SimpleNamespace(namespaceName, members);
        }
    }
}
