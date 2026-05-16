package com.fiskmods.quantify.jvm.assignable;

import com.fiskmods.quantify.jvm.VarAddress;

public record LocalVar(int id) implements VarAddress {
    @Override
    public VarType<?> type() {
        return VarType.NUM;
    }

    public static LocalVar of(final int id) {
        return new LocalVar(id);
    }
}
