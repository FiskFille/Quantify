package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.ArrayVar;
import com.fiskmods.quantify.jvm.assignable.LocalVar;

public interface Value extends Expression {
    static Value negate(final Value value) {
        return switch (value) {
            case NegatedValue(final Value v) -> v;
            case NumLiteral(final double v) -> v == 0 || Double.isNaN(v) ? value : new NumLiteral(-v);
            case LocalVar(final int id, final boolean n) -> new LocalVar(id, !n);
            case ArrayVar(final int id, final int i, final boolean n) -> new ArrayVar(id, i, !n);
            default -> new NegatedValue(value);
        };
    }

    static boolean isNegative(final Value value) {
        return value instanceof NegatedValue ||
                value instanceof NumLiteral(final double v) && v < 0 ||
                value instanceof final VarAddress var && var.isNegated();
    }
}
