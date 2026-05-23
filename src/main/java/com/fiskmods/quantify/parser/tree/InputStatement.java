package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.VarAddress;

public final class InputStatement extends Statement {
    private final int index;
    private final Identifier name;

    private final VarAddress inputAddress;
    private final VarAddress targetAddress;

    InputStatement(final int index, final Identifier name, final VarAddress inputAddress, final VarAddress targetAddress) {
        this.index = index;
        this.name = name;
        this.inputAddress = inputAddress;
        this.targetAddress = targetAddress;
    }

    public int index() {
        return index;
    }

    public Identifier name() {
        return name;
    }

    public VarAddress inputAddress() {
        return inputAddress;
    }

    public VarAddress targetAddress() {
        return targetAddress;
    }
}
