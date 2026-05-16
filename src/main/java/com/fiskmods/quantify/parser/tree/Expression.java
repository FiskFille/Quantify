package com.fiskmods.quantify.parser.tree;

public abstract non-sealed class Expression extends Tree {
    public static Expression negate(final Expression expression) {
        return switch (expression) {
            case final VarRef var -> var.negate();
            case final NumLiteral lit -> lit.negate();
            default -> NegatedExpression.of(expression);
        };
    }

    public static boolean isNegative(final Expression expression) {
        return expression instanceof NegatedExpression ||
                expression instanceof final NumLiteral lit && lit.value() < 0 ||
                expression instanceof final VarRef var && var.isNegated();
    }
}
