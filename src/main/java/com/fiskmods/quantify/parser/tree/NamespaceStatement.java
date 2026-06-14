package com.fiskmods.quantify.parser.tree;

import org.jspecify.annotations.Nullable;

public final class NamespaceStatement extends Statement {
    private final Expression expression;
    private final @Nullable Statement body;

    NamespaceStatement(final Expression expression, final @Nullable Statement body) {
        this.expression = expression;
        this.body = body;
    }

    public Expression expression() {
        return expression;
    }

    public @Nullable Statement body() {
        return body;
    }
}
