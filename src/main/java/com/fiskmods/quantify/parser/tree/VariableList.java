package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.VarAddress;

import java.util.List;

public final class VariableList extends Tree implements Assignable {
    private final List<? extends VarAddress> addresses;

    public VariableList(final List<? extends VarAddress> addresses) {
        this.addresses = addresses;
    }

    public List<? extends VarAddress> addresses() {
        return addresses;
    }
}
