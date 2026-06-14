package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.VarAddress;
import org.jspecify.annotations.Nullable;

public final class InputStatement extends Statement {
    private final int index;
    private final String name;

    public @Nullable VarAddress inputAddress;
    public @Nullable VarAddress targetAddress;

    InputStatement(final int index, final String name) {
        this.index = index;
        this.name = name;
    }

    public int index() {
        return index;
    }

    public String name() {
        return name;
    }
}
