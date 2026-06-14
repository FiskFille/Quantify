package com.fiskmods.quantify.parser.tree;

import org.jspecify.annotations.Nullable;

public final class ConstDefinitionTree extends Statement {
    private final String name;
    private final @Nullable Identifier type;
    private final Expression value;

    ConstDefinitionTree(final String name, final @Nullable Identifier type, final Expression value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String name() {
        return name;
    }

    public @Nullable Identifier type() {
        return type;
    }

    public Expression value() {
        return value;
    }
}
