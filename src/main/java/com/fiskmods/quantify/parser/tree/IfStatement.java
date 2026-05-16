package com.fiskmods.quantify.parser.tree;

import org.jspecify.annotations.Nullable;

public final class IfStatement extends Statement {
    private final Expression condition;
    private final Statement body;
    private final @Nullable Statement elseBody;

    IfStatement(final Expression condition, final Statement body, final @Nullable Statement elseBody) {
        this.condition = condition;
        this.body = body;
        this.elseBody = elseBody;
    }

    public Expression condition() {
        return condition;
    }

    public Statement body() {
        return body;
    }

    public @Nullable Statement elseBody() {
        return elseBody;
    }
}
