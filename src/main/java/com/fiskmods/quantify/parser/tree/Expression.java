package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.ArrayVar;
import com.fiskmods.quantify.jvm.assignable.LocalVar;

public interface Expression {
    static Expression negate(final Expression expression) {
        return switch (expression) {
            case final NegatedExpression neg -> neg.expression();
            case final NumLiteral lit -> {
                final double v = lit.value();
                yield v == 0 || Double.isNaN(v) ? expression : new NumLiteral(-v);
            }
            case LocalVar(final int id, final boolean n) -> new LocalVar(id, !n);
            case ArrayVar(final int id, final int i, final boolean n) -> new ArrayVar(id, i, !n);
            default -> new NegatedExpression(expression);
        };
    }

    static boolean isNegative(final Expression expression) {
        return expression instanceof NegatedExpression ||
                expression instanceof final NumLiteral lit && lit.value() < 0 ||
                expression instanceof final VarAddress var && var.isNegated();
    }
}
