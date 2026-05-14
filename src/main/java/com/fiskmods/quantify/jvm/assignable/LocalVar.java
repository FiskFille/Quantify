package com.fiskmods.quantify.jvm.assignable;

import com.fiskmods.quantify.jvm.VarAddress;

public record LocalVar(int id, boolean isNegated) implements VarAddress {

    public static LocalVar of(final int id) {
        return new LocalVar(id, false);
    }

    @Override
    public VarType<?> type() {
        return VarType.NUM;
    }
}
