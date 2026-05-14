package com.fiskmods.quantify.parser.tree;

public final class ExpressionStatement extends Tree implements Statement {
    private final Expression expression;

    public ExpressionStatement(final Expression expression) {
        this.expression = expression;
    }

    public Expression expression() {
        return expression;
    }
}
