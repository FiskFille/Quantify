package com.fiskmods.quantify.jvm.assignable;

import com.fiskmods.quantify.exception.QtfErrors;
import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.jvm.JvmUtil;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.member.MemberMap;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.parser.element.Assignable;
import com.fiskmods.quantify.parser.element.Value;
import org.objectweb.asm.MethodVisitor;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;
import java.util.function.ToIntFunction;

import static org.objectweb.asm.Opcodes.*;

public interface Struct extends Namespace, Value, Assignable {
    @Override
    default void apply(final MethodVisitor mv) {
    }

    @Override
    default void set(final MethodVisitor mv, final Value value) {
    }

    @Override
    default void modify(final MethodVisitor mv, final Value value, final Operator op) {
    }

    @Override
    default void lerp(final MethodVisitor mv, final Value value, final Value progress, final boolean rotational) {
    }

    @Override
    default FunctionAddress getFunction(final String name) throws QtfException {
        throw QtfErrors.undefined(MemberType.FUNCTION, name);
    }

    @Override
    default boolean hasFunction(final String name) {
        return false;
    }

    @Override
    default double getConstant(final String name) {
        return 0;
    }

    @Override
    default boolean hasConstant(final String name) {
        return false;
    }

    default void expand(final String name) throws QtfException {
    }

    static VarAddress<Struct> create(final int index, final IntSupplier arraySize, final ToIntFunction<String> arrayStore) {
        final Struct struct = new StructImpl(index, arraySize, arrayStore);
        return VarAddress.create(VarType.STRUCT, struct, false);
    }

    static VarAddress<Struct> create(final int index) {
        final AtomicInteger i = new AtomicInteger();
        final Struct struct = new StructImpl(index, i::get, ignored -> i.getAndIncrement());

        return VarAddress.create(VarType.STRUCT, struct, false);
    }

    class StructImpl implements Struct {
        private final MemberMap members = new MemberMap();
        private final int index;

        private final IntSupplier arraySize;
        private final ToIntFunction<String> arrayStore;

        private StructImpl(final int index, final IntSupplier arraySize, final ToIntFunction<String> arrayStore) {
            this.index = index;
            this.arraySize = arraySize;
            this.arrayStore = arrayStore;
        }

        @Override
        public void init(final MethodVisitor mv) {
            final int capacity = arraySize.getAsInt();
            JvmUtil.iconst(mv, capacity);
            mv.visitIntInsn(NEWARRAY, T_DOUBLE);
            mv.visitVarInsn(ASTORE, index);
        }

        @Override
        public void expand(final String name) throws QtfException {
            final Optional<MemberMap.Member<?>> member = members.find(name);
            if (member.isEmpty()) {
                members.putVariable(name, VarAddress.create(VarType.STRUCT, new Child(), false));
            } else {
                member.get().cast(name, MemberType.VARIABLE)
                        .value().typeCheck(name, VarType.STRUCT);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends Value & Assignable> VarAddress<T> computeVariable(final VarType<T> type, final String name, final int modifiers) throws QtfException {
            if (type == null || members.has(name)) {
                return members.get(name, MemberType.VARIABLE).cast(name, type);
            }

            if (type == VarType.STRUCT) {
                return (VarAddress<T>) members.putVariable(name, VarAddress.create(VarType.STRUCT, new Child(), false));
            } else {
                return (VarAddress<T>) members.putVariable(name, VarAddress.arrayAccess(index, arrayStore));
            }
        }

        @Override
        public boolean hasVariable(final String name) {
            return members.find(name).map(MemberType.VARIABLE::test)
                    .orElse(true);
        }

        private static class Child implements Struct {
            @Override
            public void init(final MethodVisitor mv) {
            }

            @Override
            public <T extends Value & Assignable> VarAddress<T> computeVariable(final VarType<T> type, final String name, final int modifiers) throws QtfException {
                throw QtfErrors.undefined(MemberType.VARIABLE, name);
            }

            @Override
            public boolean hasVariable(final String name) {
                return false;
            }
        }
    }
}
