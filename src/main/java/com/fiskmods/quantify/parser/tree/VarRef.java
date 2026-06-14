package com.fiskmods.quantify.parser.tree;

public final class VarRef extends Expression {
    private final Expression expression;
    private final boolean isNegated;

    VarRef(final Expression expression, final boolean isNegated) {
        this.expression = expression;
        this.isNegated = isNegated;
    }

    public VarRef negate() {
        final VarRef var = new VarRef(expression, isNegated);
        var.range = range;
        return var;
    }

    public Expression expression() {
        return expression;
    }

    public boolean isNegated() {
        return isNegated;
    }
}
