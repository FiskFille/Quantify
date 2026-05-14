package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.library.QtfMath;

import java.util.List;

public record Operation(
        Value left,
        Value right,
        Operator op
) implements Value {
    public static Value wrap(final Value left, final Value right, final Operator op) {
        switch (op) {
            case MUL, AND -> {
                // Any multiplication where one term is 0 or 1 is redundant
                if (left instanceof NumLiteral(final double value)) {
                    if (value == 0) return left;
                    if (value == 1) return right;
                }
                if (right instanceof NumLiteral(final double value)) {
                    if (value == 0) return right;
                    if (value == 1) return left;
                }

                if (Value.isNegative(left) && Value.isNegative(right)) {
                    return wrap(Value.negate(left), Value.negate(right), op);
                }
            }
            case DIV -> {
                if (left instanceof NumLiteral(final double value) && value == 0) {
                    // Any division where the dividend is 0 is redundant
                    return left;
                }
                if (right instanceof NumLiteral(final double value) && value == 1) {
                    // Any division where the divisor is 1 is redundant
                    return left;
                }
            }
            case POW -> {
                if (right instanceof NumLiteral(final double exponent)) {
                    if (exponent == 1) return left; // x^y=x for y=1
                    else if (VarAddress.isVar(left)) {
                        if (exponent == 2) return wrap(left, left, Operator.MUL);
                        if (exponent == 3) return wrap(left, wrap(left, left, Operator.MUL), Operator.MUL);
                    } else if (!(left instanceof NumLiteral)) {
                        if (exponent == 2) return new FunctionRef(QtfMath.SQUARE, List.of(left));
                        if (exponent == 3) return new FunctionRef(QtfMath.CUBE, List.of(left));
                    }
                }
            }
        }
        if (left instanceof NumLiteral(final double l) && right instanceof NumLiteral(final double r)) {
            // Pre-compute literal arithmetic
            return new NumLiteral(op.applyAsDouble(l, r));
        }
        return new Operation(left, right, op);
    }
}
