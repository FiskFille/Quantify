package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.FunctionAddress;

import java.util.List;

public final class FunctionRef extends Tree implements Expression {
    private final FunctionAddress address;
    private final List<? extends Expression> args;

    public FunctionRef(final FunctionAddress address, final List<? extends Expression> args) {
        this.address = address;
        this.args = args;
    }

    public FunctionAddress address() {
        return address;
    }

    public List<? extends Expression> args() {
        return args;
    }
}
