package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.VarType;

public final class ParameterTree extends Statement {
    private final Identifier name;
    private final VarType<?> type;
    private final VarAddress address;

    ParameterTree(final Identifier name, final VarType<?> type, final VarAddress address) {
        this.name = name;
        this.type = type;
        this.address = address;
    }

    public Identifier name() {
        return name;
    }

    public VarType<?> type() {
        return type;
    }

    public VarAddress address() {
        return address;
    }
}
