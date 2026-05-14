package com.fiskmods.quantify.parser.tree;

public final class NumLiteral extends Tree implements Expression {
    private final double value;

    public NumLiteral(final double value) {
        this.value = value;
    }

    public double value() {
        return value;
    }
}
