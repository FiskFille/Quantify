package com.fiskmods.quantify.parser.tree;

public final class ExpressionStatement extends Statement {
    private final Expression expression;

    private ExpressionStatement(final Expression expression) {
        this.expression = expression;
    }

    public static ExpressionStatement of(final Expression expression) {
        final ExpressionStatement statement = new ExpressionStatement(expression);
        statement.range = expression.range();
        return statement;
    }

    public Expression expression() {
        return expression;
    }
}
