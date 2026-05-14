package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.library.QtfMath;

import java.util.List;

public final class Operation extends Tree implements Expression {
    private final Expression left;
    private final Expression right;
    private final Operator op;

    public Operation(final Expression left, final Expression right, final Operator op) {
        this.left = left;
        this.right = right;
        this.op = op;
    }

    public Expression left() {
        return left;
    }

    public Expression right() {
        return right;
    }

    public Operator op() {
        return op;
    }

    public static Expression wrap(final Expression left, final Expression right, final Operator op) {
        switch (op) {
            case MUL, AND -> {
                // Any multiplication where one term is 0 or 1 is redundant
                if (left instanceof final NumLiteral l) {
                    if (l.value() == 0) return left;
                    if (l.value() == 1) return right;
                }
                if (right instanceof final NumLiteral r) {
                    if (r.value() == 0) return right;
                    if (r.value() == 1) return left;
                }

                if (Expression.isNegative(left) && Expression.isNegative(right)) {
                    return wrap(Expression.negate(left), Expression.negate(right), op);
                }
            }
            case DIV -> {
                if (left instanceof final NumLiteral lit && lit.value() == 0) {
                    // Any division where the dividend is 0 is redundant
                    return left;
                }
                if (right instanceof final NumLiteral lit && lit.value() == 1) {
                    // Any division where the divisor is 1 is redundant
                    return left;
                }
            }
            case POW -> {
                if (right instanceof final NumLiteral lit) {
                    final double exponent = lit.value();
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
        if (left instanceof final NumLiteral l && right instanceof final NumLiteral r) {
            // Pre-compute literal arithmetic
            return new NumLiteral(op.applyAsDouble(l.value(), r.value()));
        }
        return new Operation(left, right, op);
    }
}
