package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.VarAddress;

public final class VarRef extends Expression {
    private final Expression expression;
    private final VarAddress address;
    private final boolean isNegated;

    VarRef(final Expression expression, final VarAddress address, final boolean isNegated) {
        this.expression = expression;
        this.address = address;
        this.isNegated = isNegated;
    }

    public VarRef negate() {
        final VarRef var = new VarRef(expression, address, !isNegated);
        var.range = range;
        return var;
    }

    public Expression expression() {
        return expression;
    }

    public VarAddress address() {
        return address;
    }

    public boolean isNegated() {
        return isNegated;
    }
}
