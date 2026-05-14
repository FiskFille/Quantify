package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.VarAddress;
import org.jspecify.annotations.Nullable;

public final class InterpolateStatement extends Tree implements Statement {
    private final Expression progress;
    private final @Nullable VarAddress substitution;
    private final Statement body;

    public InterpolateStatement(final Expression progress, final @Nullable VarAddress substitution, final Statement body) {
        this.progress = progress;
        this.substitution = substitution;
        this.body = body;
    }

    public Expression progress() {
        return progress;
    }

    public @Nullable VarAddress substitution() {
        return substitution;
    }

    public Statement body() {
        return body;
    }
}
