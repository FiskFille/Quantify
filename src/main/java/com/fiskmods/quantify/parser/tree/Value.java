package com.fiskmods.quantify.parser.tree;

public interface Value extends Tree {
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
