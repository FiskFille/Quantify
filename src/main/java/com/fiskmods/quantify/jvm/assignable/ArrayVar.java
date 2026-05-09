package com.fiskmods.quantify.jvm.assignable;

import com.fiskmods.quantify.jvm.VarAddress;

import java.util.function.Function;
import java.util.function.ToIntFunction;

public record ArrayVar(int id, int arrayIndex, boolean isNegated) implements VarAddress {

    public static ArrayVar of(final int id, final int arrayIndex) {
        return new ArrayVar(id, arrayIndex, false);
    }

    public static <T> Function<T, ArrayVar> of(final int id, final ToIntFunction<T> arrayIndex) {
        return t -> of(id, arrayIndex.applyAsInt(t));
    }

    @Override
    public VarType<?> type() {
        return VarType.NUM;
    }

    @Override
    public ArrayVar negate() {
        return new ArrayVar(id, arrayIndex, !isNegated);
    }
}
