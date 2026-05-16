package com.fiskmods.quantify.jvm;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.parser.tree.Expression;
import com.fiskmods.quantify.parser.tree.NegatedExpression;
import com.fiskmods.quantify.parser.tree.VarRef;

public interface VarAddress {
    VarType<?> type();

    default boolean is(final VarType<?> type) {
        return type() == type;
    }

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

    static boolean isVar(Expression expression) {
        while (expression instanceof final NegatedExpression neg) {
            expression = neg.expression();
        }
        return expression instanceof VarRef;
    }
}
