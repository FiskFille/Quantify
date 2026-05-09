package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.FunctionAddress;

import java.util.Arrays;
import java.util.List;

public record FunctionRef(
        FunctionAddress address,
        List<? extends Value> args,
        boolean hasResult
) implements Value {
    static FunctionRef call(final FunctionAddress address, final Value... args) {
        return new FunctionRef(address, Arrays.asList(args), true);
    }

    static FunctionRef run(final FunctionAddress address, final Value... args) {
        return new FunctionRef(address, Arrays.asList(args), false);
    }
}
