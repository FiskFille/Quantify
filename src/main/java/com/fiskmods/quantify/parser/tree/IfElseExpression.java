package com.fiskmods.quantify.parser.tree;

public final class IfElseExpression extends Expression {
    private final Expression condition;
    private final Expression thenExpression;
    private final Expression elseExpression;

    IfElseExpression(final Expression condition, final Expression thenExpression, final Expression elseExpression) {
        this.condition = condition;
        this.thenExpression = thenExpression;
        this.elseExpression = elseExpression;
    }

    public Expression condition() {
        return condition;
    }

    public Expression thenExpression() {
        return thenExpression;
    }

    public Expression elseExpression() {
        return elseExpression;
    }
}
