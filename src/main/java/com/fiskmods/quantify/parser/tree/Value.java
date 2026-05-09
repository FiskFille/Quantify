package com.fiskmods.quantify.parser.tree;

public interface Value extends Expression {
    default Value negate() {
        return new NegatedValue(this);
    }

    default Value negateIf(final boolean shouldNegate) {
        return shouldNegate ? negate() : this;
    }

    default boolean isNegated() {
        return false;
    }
}
