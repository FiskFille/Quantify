package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.VarAddress;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class LerpAssignment extends Statement {
    private final List<VarRef> targets;
    private final Expression value;
    private final boolean rotational;

    public @Nullable VarAddress progress;

    LerpAssignment(final List<VarRef> targets, final Expression value, final boolean rotational) {
        this.targets = targets;
        this.value = value;
        this.rotational = rotational;
    }

    public List<VarRef> targets() {
        return targets;
    }

    public Expression value() {
        return value;
    }

    public boolean rotational() {
        return rotational;
    }
}
