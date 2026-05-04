package com.fiskmods.quantify.member;

import com.fiskmods.quantify.exception.QtfErrors;
import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.VarType;

import java.util.HashMap;
import java.util.Map;

public final class SimpleNamespace implements Namespace {
    private final String namespaceName;

    private final Map<String, FunctionAddress> functions;
    private final Map<String, Double> constants;

    public SimpleNamespace(final String namespaceName, final Map<String, FunctionAddress> functions, final Map<String, Double> constants) {
        this.namespaceName = namespaceName;
        this.functions = new HashMap<>(functions);
        this.constants = new HashMap<>(constants);
    }

    @Override
    public <T extends VarAddress> T computeVariable(final VarType<T> type, final String name, final int modifiers) throws QtfException {
        throw QtfErrors.undefined(MemberType.VARIABLE, name, namespaceName);
    }

    @Override
    public boolean hasVariable(final String name) {
        return false;
    }

    @Override
    public FunctionAddress getFunction(final String name) throws QtfException {
        final FunctionAddress func = functions.get(name);
        if (func == null) {
            throw QtfErrors.undefined(MemberType.FUNCTION, name, namespaceName);
        }
        return func;
    }

    @Override
    public boolean hasFunction(final String name) {
        return functions.containsKey(name);
    }

    @Override
    public double getConstant(final String name) throws QtfException {
        final Double constant = constants.get(name);
        if (constant == null) {
            throw QtfErrors.undefined(MemberType.CONSTANT, name, namespaceName);
        }
        return constant;
    }

    @Override
    public boolean hasConstant(final String name) {
        return constants.containsKey(name);
    }

    public static Builder builder(final String name) {
        return new Builder(name);
    }

    public static final class Builder {
        private final String namespaceName;

        private final Map<String, FunctionAddress> functions = new HashMap<>();
        private final Map<String, Double> constants = new HashMap<>();

        private Builder(final String namespaceName) {
            this.namespaceName = namespaceName;
        }

        public Builder addConstant(final String name, final double value) {
            functions.remove(name);
            constants.put(name, value);
            return this;
        }

        public Builder addFunction(final String name, final FunctionAddress function) {
            constants.remove(name);
            functions.put(name, function);
            return this;
        }

        public Builder addFunction(final FunctionAddress function) {
            return addFunction(function.name(), function);
        }

        public Builder addFunction(final String owner, final String name, final int parameters) {
            return addFunction(name, FunctionAddress.create(owner, name, parameters));
        }

        public Namespace build() {
            return new SimpleNamespace(namespaceName, functions, constants);
        }
    }
}
