package com.fiskmods.quantify.parser.tree;

public final class LerpAssignment extends Tree implements Statement {
    private final Assignable target;
    private final Expression value;
    private final Expression progress;
    private final boolean rotational;

    public LerpAssignment(final Assignable target, final Expression value, final Expression progress, final boolean rotational) {
        this.target = target;
        this.value = value;
        this.progress = progress;
        this.rotational = rotational;
    }

    public Assignable target() {
        return target;
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
