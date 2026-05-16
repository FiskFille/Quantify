package com.fiskmods.quantify.parser.tree;

public final class NumLiteral extends Expression {
    private final double value;

    NumLiteral(final double value) {
        this.value = value;
    }

    public NumLiteral negate() {
        if (value == 0 || Double.isNaN(value)) {
            return this;
        }

        final NumLiteral lit = new NumLiteral(-value);
        lit.range = range;
        return lit;
    }

    public double value() {
        return value;
    }
}
