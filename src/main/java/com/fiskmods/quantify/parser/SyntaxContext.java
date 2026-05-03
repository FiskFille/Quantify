package com.fiskmods.quantify.parser;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.jvm.JvmClassComposer;
import com.fiskmods.quantify.jvm.JvmFunctionDefinition;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.NumVar;
import com.fiskmods.quantify.jvm.assignable.Struct;
import com.fiskmods.quantify.jvm.assignable.VarInfo;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.library.LibraryMap;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.member.Scope;
import com.fiskmods.quantify.member.ScopeProvider;
import com.fiskmods.quantify.parser.element.Assignable;
import com.fiskmods.quantify.parser.element.Value;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SyntaxContext implements ScopeProvider {
    private static final int INPUT_ID = 1;
    private static final int OUTPUT_ID = 2;

    private final Namespace defaultNamespace = new DefaultNamespace();

    private final Scope globalScope = new Scope(defaultNamespace, 0);
    private final LinkedList<Scope> currentScope = new LinkedList<>();

    private final Map<String, Integer> inputs = new HashMap<>();
    private final List<JvmFunctionDefinition> functionDefinitions = new ArrayList<>();

    private final List<String> outputs = new ArrayList<>();
    private final AtomicInteger outputIndex = new AtomicInteger();

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

    public VarAddress<NumVar> addInputVariable(final String name, final int index) throws QtfException {
        final VarAddress<NumVar> var = globalScope.members.put("in:" + name,
                () -> VarAddress.arrayAccess(INPUT_ID, index));
        inputs.put(name, index);
        return var;
    }

    @SuppressWarnings("unchecked")
    public <T extends Value & Assignable> VarAddress<T> addPublicVar(final String name, final VarType<T> type) throws QtfException {
        if (type == VarType.STRUCT) {
            return (VarAddress<T>) scope().members.put(name, () -> VarAddress.create(VarType.STRUCT,
                    Struct.create(name, OUTPUT_ID, outputIndex, outputs), false));
        }
        final VarAddress<NumVar> var = globalScope.members.put(name,
                () -> VarAddress.arrayAccess(OUTPUT_ID, outputIndex.getAndIncrement()));
        outputs.add(name);
        return (VarAddress<T>) var;
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

    public Map<String, Integer> getInputs() {
        return inputs;
    }

    public List<String> getOutputs() {
        return outputs;
    }

    public JvmClassComposer createClassComposer(final String className) {
        return functionDefinitions.stream()
                .map(t -> t.define(className))
                .reduce(JvmClassComposer.DO_NOTHING, JvmClassComposer::andThen);
    }

    private class DefaultNamespace implements Namespace {
        @Override
        public <T extends Value & Assignable> VarAddress<T> computeVariable(final VarType<T> type, final String name, final int modifiers) throws QtfException {
            if ((modifiers & VarInfo.DEFINITION) != 0) {
                return VarInfo.define(name, type, SyntaxContext.this, modifiers);
            }
            return getMember(name, MemberType.VARIABLE)
                    .cast(name, type);
        }

        @Override
        public boolean hasVariable(final String name) {
            return hasMember(name, MemberType.VARIABLE);
        }

        @Override
        public FunctionAddress getFunction(final String name) throws QtfException {
            return getMember(name, MemberType.FUNCTION);
        }

        @Override
        public boolean hasFunction(final String name) {
            return hasMember(name, MemberType.FUNCTION);
        }

        @Override
        public double getConstant(final String name) throws QtfException {
            return getMember(name, MemberType.CONSTANT);
        }

        @Override
        public boolean hasConstant(final String name) {
            return hasMember(name, MemberType.CONSTANT);
        }
    }
}
