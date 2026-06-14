package com.fiskmods.quantify.jvm.assignable;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.member.MemberMap;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.member.MutableMemberMap;
import com.fiskmods.quantify.member.Namespace;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;
import java.util.function.ToIntFunction;

public interface Struct extends VarAddress, Namespace {
    @Override
    default VarType<Struct> type() {
        return VarType.STRUCT;
    }

    static Struct of(final int index, final IntSupplier arraySize, final ToIntFunction<String> arrayStore) {
        return new StructImpl(index, arraySize, arrayStore);
    }

    static Struct of(final int index) {
        final AtomicInteger i = new AtomicInteger();
        return new StructImpl(index, i::get, ignored -> i.getAndIncrement());
    }

    class StructImpl implements Struct {
        private final MutableMemberMap members = new MutableMemberMap();
        private final int index;

        private final IntSupplier arraySize;
        private final ToIntFunction<String> arrayStore;

        private StructImpl(final int index, final IntSupplier arraySize, final ToIntFunction<String> arrayStore) {
            this.index = index;
            this.arraySize = arraySize;
            this.arrayStore = arrayStore;
        }

        public int index() {
            return index;
        }

        public int size() {
            return arraySize.getAsInt();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends VarAddress> T computeVariable(final VarType<T> type, final String name) throws QtfException {
            if (members.has(name)) {
                return Struct.super.computeVariable(type, name);
            }

            if (type == VarType.STRUCT) {
                return (T) members.putVariable(name, new ChildStruct(this, name));
            } else {
                return (T) members.putVariable(name, ArrayVar.of(index, arrayStore));
            }
        }

        @Override
        public Optional<MemberMap.Member<?>> find(final String name) {
            return members.find(name);
        }

        @Override
        public <T> Optional<T> find(final String name, final MemberType<T> expectedType) {
            return members.find(name, expectedType);
        }

        @Override
        public boolean has(final String name) {
            return members.has(name);
        }

        @Override
        public boolean has(final String name, final MemberType<?> expectedType) {
            return members.has(name, expectedType);
        }

        @Override
        public <T> T get(final String name, final MemberType<T> expectedType) throws QtfException {
            return members.get(name, expectedType);
        }

        private record ChildStruct(StructImpl root, String name) implements Struct {
            @Override
            public <T extends VarAddress> T computeVariable(final VarType<T> type, final String name) throws QtfException {
                return root.computeVariable(type, getMemberName(name));
            }

            @Override
            public Optional<MemberMap.Member<?>> find(final String name) {
                return root.find(getMemberName(name));
            }

            @Override
            public <T> Optional<T> find(final String name, final MemberType<T> expectedType) {
                return root.find(getMemberName(name), expectedType);
            }

            @Override
            public boolean has(final String name) {
                return root.has(getMemberName(name));
            }

            @Override
            public boolean has(final String name, final MemberType<?> expectedType) {
                return root.has(getMemberName(name), expectedType);
            }

            @Override
            public <T> T get(final String name, final MemberType<T> expectedType) throws QtfException {
                return root.get(getMemberName(name), expectedType);
            }

            private String getMemberName(final String name) {
                return this.name + '.' + name;
            }
        }
    }
}
