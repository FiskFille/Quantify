package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.member.Namespace;
import org.jspecify.annotations.Nullable;

public final class NamespaceStatement extends Statement {
    private final Expression expression;
    private final Namespace namespace;
    private final @Nullable Statement body;

    NamespaceStatement(final Expression expression, final Namespace namespace, final @Nullable Statement body) {
        this.expression = expression;
        this.namespace = namespace;
        this.body = body;
    }

    public Expression expression() {
        return expression;
    }

    public Namespace namespace() {
        return namespace;
    }

    public @Nullable Statement body() {
        return body;
    }
}
