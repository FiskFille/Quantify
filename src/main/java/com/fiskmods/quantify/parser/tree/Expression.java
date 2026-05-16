package com.fiskmods.quantify.parser.tree;

public abstract non-sealed class Expression extends Tree {
    public static Expression negate(final Expression expression) {
        return switch (expression) {
            case final NegatedExpression neg -> neg.expression();
            case final VarRef var -> new VarRef(var.address(), !var.isNegated());
            case final NumLiteral lit -> {
                final double v = lit.value();
                yield v == 0 || Double.isNaN(v) ? expression : new NumLiteral(-v);
            }
            default -> new NegatedExpression(expression);
        };
    }

    public static boolean isNegative(final Expression expression) {
        return expression instanceof NegatedExpression ||
                expression instanceof final NumLiteral lit && lit.value() < 0 ||
                expression instanceof final VarRef var && var.isNegated();
    }
}
