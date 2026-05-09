package com.fiskmods.quantify.parser.tree;

public record NumLiteral(double value) implements Value {
    @Override
    public boolean isNegated() {
        return value < 0;
    }

    @Override
    public Value negate() {
        if (value == 0 || Double.isNaN(value)) {
            return this;
        }
        return new NumLiteral(-value);
    }
}
