package com.fiskmods.quantify.parser.tree;

import java.util.List;

public final class LerpAssignment extends Statement {
    private final List<? extends VarRef> targets;
    private final Expression value;
    private final Expression progress;
    private final boolean rotational;

    public LerpAssignment(final List<? extends VarRef> targets, final Expression value, final Expression progress, final boolean rotational) {
        this.targets = targets;
        this.value = value;
        this.progress = progress;
        this.rotational = rotational;
    }

    public List<? extends VarRef> targets() {
        return targets;
    }

    public Expression value() {
        return value;
    }

    public Expression progress() {
        return progress;
    }

    public boolean rotational() {
        return rotational;
    }
}
