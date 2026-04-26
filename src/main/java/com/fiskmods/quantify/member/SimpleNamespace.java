package com.fiskmods.quantify.member;

import com.fiskmods.quantify.exception.QtfErrors;
import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.parser.element.Assignable;
import com.fiskmods.quantify.parser.element.Value;

import java.util.HashMap;
import java.util.Map;

public final class SimpleNamespace implements Namespace {
    private final String namespaceName;

    private final Map<String, FunctionAddress> functions;
    private final Map<String, Double> constants;

    public SimpleNamespace(String namespaceName, Map<String, FunctionAddress> functions, Map<String, Double> constants) {
        this.namespaceName = namespaceName;
        this.functions = new HashMap<>(functions);
        this.constants = new HashMap<>(constants);
    }

    @Override
    public <T extends Value & Assignable> VarAddress<T> computeVariable(VarType<T> type, String name, int modifiers) throws QtfException {
        throw QtfErrors.undefined(MemberType.VARIABLE, name, namespaceName);
    }

    @Override
    public boolean hasVariable(String name) {
        return false;
    }

    @Override
    public FunctionAddress getFunction(String name) throws QtfException {
        FunctionAddress func = functions.get(name);
        if (func == null) {
            throw QtfErrors.undefined(MemberType.FUNCTION, name, namespaceName);
        }
        return func;
    }

    @Override
    public boolean hasFunction(String name) {
        return functions.containsKey(name);
    }

    @Override
    public double getConstant(String name) throws QtfException {
        Double constant = constants.get(name);
        if (constant == null) {
            throw QtfErrors.undefined(MemberType.CONSTANT, name, namespaceName);
        }
        return constant;
    }

    @Override
    public boolean hasConstant(String name) {
        return constants.containsKey(name);
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static final class Builder {
        private final String namespaceName;

        private final Map<String, FunctionAddress> functions = new HashMap<>();
        private final Map<String, Double> constants = new HashMap<>();

        private Builder(String namespaceName) {
            this.namespaceName = namespaceName;
        }

        public Builder addConstant(String name, double value) {
            functions.remove(name);
            constants.put(name, value);
            return this;
        }

        public Builder addFunction(String name, FunctionAddress function) {
            constants.remove(name);
            functions.put(name, function);
            return this;
        }

        public Builder addFunction(String owner, String name, int parameters) {
            return addFunction(name, FunctionAddress.create(owner, name, parameters));
        }

        public Namespace build() {
            return new SimpleNamespace(namespaceName, functions, constants);
        }
    }
}
