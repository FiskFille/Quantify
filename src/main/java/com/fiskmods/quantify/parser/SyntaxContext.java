package com.fiskmods.quantify.parser;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.jvm.JvmFunctionDefinition;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.ArrayVar;
import com.fiskmods.quantify.jvm.assignable.Struct;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.library.LibraryMap;
import com.fiskmods.quantify.member.*;
import com.fiskmods.quantify.util.IndexMap;

import java.util.*;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class SyntaxContext implements ScopeProvider {
    private static final int INPUT_ID = 1;
    private static final int OUTPUT_ID = 2;

    private final Namespace defaultNamespace = new DefaultNamespace();

    private final Scope globalScope = new Scope(defaultNamespace, 0);
    private final LinkedList<Scope> currentScope = new LinkedList<>();

    private final Map<String, Integer> inputs = new HashMap<>();
    private final List<JvmFunctionDefinition> functionDefinitions = new ArrayList<>();

    private final Outputs outputs = new Outputs();

    private final LibraryMap libraries;

    public SyntaxContext(final LibraryMap libraries) {
        this.libraries = libraries;
        currentScope.add(globalScope);
    }

    public LibraryMap libraries() {
        return libraries;
    }

    public Namespace namespace() {
        return scope().getNamespace();
    }

    public Namespace getDefaultNamespace() {
        return defaultNamespace;
    }

    @Override
    public Scope scope() {
        return currentScope.getLast();
    }

    @Override
    public Scope global() {
        return globalScope;
    }

    @Override
    public void push(final Scope scope) {
        currentScope.add(scope);
    }

    @Override
    public void pop() {
        if (currentScope.size() > 1) {
            currentScope.removeLast();
        }
    }

    @Override
    public int stackDepth() {
        return currentScope.size();
    }

    public ArrayVar addInputVariable(final String name, final int index) throws QtfException {
        return globalScope.members.<ArrayVar>putVariable("in:" + name, () -> {
            final ArrayVar var = ArrayVar.of(INPUT_ID, index);
            inputs.put(name, index);
            return var;
        });
    }

    @SuppressWarnings("unchecked")
    public <T extends VarAddress> T addPublicVar(final String name, final VarType<T> type) throws QtfException {
        if (type == VarType.STRUCT) {
            return (T) globalScope.members.putVariable(name, Struct.of(OUTPUT_ID, outputs::size, outputs.storePrefixed(name)));
        } else {
            return (T) globalScope.members.putVariable(name, ArrayVar.of(OUTPUT_ID, outputs::store));
        }
    }

    public int defineFunction(final JvmFunctionDefinition definition) {
        functionDefinitions.add(definition);
        return functionDefinitions.size() - 1;
    }

    public Map<String, FunctionAddress> getFunctions() {
        return functionDefinitions.stream()
                .filter(JvmFunctionDefinition::isVisible)
                .collect(Collectors.toMap(
                        JvmFunctionDefinition::name,
                        JvmFunctionDefinition::address
                ));
    }

    public IndexMap<String> getInputs() {
        return IndexMap.of(Collections.unmodifiableMap(inputs));
    }

    public IndexMap<String> getOutputs() {
        return IndexMap.of(Collections.unmodifiableList(outputs));
    }

    private class DefaultNamespace implements Namespace {
        @Override
        public Optional<MemberMap.Member<?>> find(final String name) {
            return scope().members.find(name);
        }

        @Override
        public <T> Optional<T> find(final String name, final MemberType<T> expectedType) {
            return expectedType.scope(SyntaxContext.this).members.find(name, expectedType);
        }

        @Override
        public boolean has(final String name) {
            return scope().members.has(name);
        }

        @Override
        public boolean has(final String name, final MemberType<?> expectedType) {
            return expectedType.scope(SyntaxContext.this).members.has(name, expectedType);
        }

        @Override
        public <T> T get(final String name, final MemberType<T> expectedType) throws QtfException {
            return expectedType.scope(SyntaxContext.this).members.get(name, expectedType);
        }
    }

    private static class Outputs extends ArrayList<String> {
        public int store(final String name) {
            add(name);
            return size() - 1;
        }

        public ToIntFunction<String> storePrefixed(final String prefix) {
            return name -> store(prefix + '.' + name);
        }
    }
}
