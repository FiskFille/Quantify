package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.VarAddress;

public final class VarRef extends Expression {
    private final VarAddress address;
    private final boolean isNegated;

    VarRef(final VarAddress address, final boolean isNegated) {
        this.address = address;
        this.isNegated = isNegated;
    }

    public VarRef negate() {
        final VarRef var = new VarRef(address, !isNegated);
        var.range = range;
        return var;
    }

    public VarAddress address() {
        return address;
    }

    public boolean isNegated() {
        return isNegated;
    }
}
