package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.VarAddress;
import org.jspecify.annotations.Nullable;

public final class InterpolateStatement extends Statement {
    private final Expression progress;
    private final Statement body;

    public @Nullable VarAddress progressAddress;

    InterpolateStatement(final Expression progress, final Statement body) {
        this.progress = progress;
        this.body = body;
    }

    public Expression progress() {
        return progress;
    }

    public Statement body() {
        return body;
    }
}
