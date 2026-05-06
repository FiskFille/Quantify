package com.fiskmods.quantify.jvm;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.parser.tree.Assignable;
import com.fiskmods.quantify.parser.tree.Value;

public interface VarAddress extends Value, Assignable {
    VarType<?> type();

    default boolean is(final VarType<?> type) {
        return type() == type;
    }

    @Override
    boolean isNegated();

    @Override
    VarAddress negate();

    default void typeCheck(final String name, final VarType<?> expectedType) throws QtfException {
        if (expectedType != null && type() != expectedType) {
            throw new QtfException("Expected '%s' to be of type %s, was %s".formatted(name, expectedType.typeName(), type().typeName()));
        }
    }

    @SuppressWarnings("unchecked")
    default <U extends VarAddress> U cast(final String name, final VarType<U> expectedType) throws QtfException {
        typeCheck(name, expectedType);
        return (U) this;
    }

    static boolean isVar(Value value) {
        while (value instanceof NegatedValue(final Value val)) {
            value = val;
        }
        return value instanceof VarAddress;
    }
}
