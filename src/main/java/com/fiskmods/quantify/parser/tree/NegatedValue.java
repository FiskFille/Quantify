package com.fiskmods.quantify.parser.tree;

public record NegatedValue(Value val) implements Value {
    @Override
    public boolean isNegated() {
        return true;
    }

    @Override
    public Value negate() {
        return val;
    }
}
