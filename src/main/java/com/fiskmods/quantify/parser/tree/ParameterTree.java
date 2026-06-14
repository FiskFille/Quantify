package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.assignable.VarType;

public final class ParameterTree extends Statement {
    private final String name;
    private final VarType<?> type;

    ParameterTree(final String name, final VarType<?> type) {
        this.name = name;
        this.type = type;
    }

    public String name() {
        return name;
    }

    public VarType<?> type() {
        return type;
    }
}
