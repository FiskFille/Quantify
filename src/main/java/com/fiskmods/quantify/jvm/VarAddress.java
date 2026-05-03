package com.fiskmods.quantify.jvm;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.assignable.NumVar;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.parser.element.Assignable;
import com.fiskmods.quantify.parser.element.Value;
import org.objectweb.asm.MethodVisitor;

public interface VarAddress<T extends Value & Assignable> extends Value, Assignable {
    VarType<T> type();

    default boolean is(final VarType<?> type) {
        return type() == type;
    }

    T access();

    boolean isNegated();

    default void typeCheck(final String name, final VarType<?> expectedType) throws QtfException {
        if (expectedType != null && type() != expectedType) {
            throw new QtfException("Expected '%s' to be of type %s, was %s".formatted(name, expectedType.typeName(), type().typeName()));
        }
    }

    @SuppressWarnings("unchecked")
    default <U extends Value & Assignable> VarAddress<U> cast(final String name, final VarType<U> expectedType) throws QtfException {
        typeCheck(name, expectedType);
        return (VarAddress<U>) this;
    }

    @Override
    default void apply(final MethodVisitor mv) {
        access().apply(mv);
    }

    @Override
    default void modify(final MethodVisitor mv, final Value value, final Operator operator) {
        access().modify(mv, value, operator);
    }

    @Override
    default void set(final MethodVisitor mv, final Value value) {
        access().set(mv, value);
    }

    @Override
    default void init(final MethodVisitor mv) {
        access().init(mv);
    }

    @Override
    default void lerp(final MethodVisitor mv, final Value value, final Value progress, final boolean rotational) {
        access().lerp(mv, value, progress, rotational);
    }

    static boolean isVar(Value value) {
        while (value instanceof NegatedValue(final Value val)) {
            value = val;
        }
        return value instanceof VarAddress<?>;
    }

    static <T extends Value & Assignable> VarAddress<T> create(final VarType<T> type, final T access, final boolean isNegated) {
        return new Impl<>(type, access, isNegated);
    }

    static <T extends Value & Assignable> VarAddress<T> create(final VarAddress<T> other, final boolean isNegated) {
        return create(other.type(), other.access(), isNegated);
    }

    static VarAddress<NumVar> local(final int id) {
        return new Impl<>(VarType.NUM, new NumVar.Local(id), false);
    }

    static VarAddress<NumVar> arrayAccess(final int id, final int arrayIndex) {
        return new Impl<>(VarType.NUM, new NumVar.ArrayAccess(id, arrayIndex), false);
    }

    record Impl<T extends Value & Assignable>(VarType<T> type, T access, boolean isNegated) implements VarAddress<T> {
    }
}
