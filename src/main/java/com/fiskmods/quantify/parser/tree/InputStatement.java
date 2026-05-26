package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.VarAddress;

public final class InputStatement extends Statement {
    private final int index;
    private final Identifier name;

    private final VarRef inputVar;
    private final VarAddress targetAddress;

    InputStatement(final int index, final Identifier name, final VarRef inputVar, final VarAddress targetAddress) {
        this.index = index;
        this.name = name;
        this.inputVar = inputVar;
        this.targetAddress = targetAddress;
    }

    public int index() {
        return index;
    }

    public Identifier name() {
        return name;
    }

    public VarRef inputVar() {
        return inputVar;
    }

    public VarAddress targetAddress() {
        return targetAddress;
    }
}
