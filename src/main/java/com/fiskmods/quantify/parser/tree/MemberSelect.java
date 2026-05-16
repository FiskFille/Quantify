package com.fiskmods.quantify.parser.tree;

public final class MemberSelect extends Expression {
    private final Expression expression;
    private final String identifier;

    MemberSelect(final Expression expression, final String identifier) {
        this.expression = expression;
        this.identifier = identifier;
    }

    public Expression expression() {
        return expression;
    }

    public String identifier() {
        return identifier;
    }
}
