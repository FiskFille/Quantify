package com.fiskmods.quantify.parser.tree;

import org.jspecify.annotations.Nullable;

public final class ParameterTree extends Statement {
    private final String name;
    private final @Nullable Identifier type;

    ParameterTree(final String name, final @Nullable Identifier type) {
        this.name = name;
        this.type = type;
    }

    public String name() {
        return name;
    }

    public @Nullable Identifier type() {
        return type;
    }
}
