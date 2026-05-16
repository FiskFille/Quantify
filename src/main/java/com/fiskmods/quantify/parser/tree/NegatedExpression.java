package com.fiskmods.quantify.parser.tree;

public final class NegatedExpression extends Expression {
    private final Expression expression;

    private NegatedExpression(final Expression expression) {
        this.expression = expression;
    }

    public static Expression of(final Expression expression) {
        if (expression instanceof final NegatedExpression neg) {
            return neg.expression;
        }

        final NegatedExpression neg = new NegatedExpression(expression);
        neg.range = expression.range;
        return neg;
    }

    public Expression expression() {
        return expression;
    }
}
