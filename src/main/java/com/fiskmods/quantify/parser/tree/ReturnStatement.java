package com.fiskmods.quantify.parser.tree;

public final class ReturnStatement extends Tree implements Statement {
    private final Expression expression;

    public ReturnStatement(final Expression expression) {
        this.expression = expression;
    }

    public Expression expression() {
        return expression;
    }
}
