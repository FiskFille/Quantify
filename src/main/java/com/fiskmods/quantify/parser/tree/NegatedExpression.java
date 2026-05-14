package com.fiskmods.quantify.parser.tree;

public final class NegatedExpression extends Tree implements Expression {
    private final Expression expression;

    public NegatedExpression(final Expression expression) {
        this.expression = expression;
    }

    public Expression expression() {
        return expression;
    }
}
