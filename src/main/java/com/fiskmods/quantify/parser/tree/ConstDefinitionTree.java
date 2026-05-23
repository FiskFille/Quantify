package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.assignable.VarType;

public final class ConstDefinitionTree extends Statement {
    private final Identifier name;
    private final VarType<?> type;
    private final Expression value;

    ConstDefinitionTree(final Identifier name, final VarType<?> type, final Expression value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public Identifier name() {
        return name;
    }

    public VarType<?> type() {
        return type;
    }

    public Expression value() {
        return value;
    }
}
