package com.fiskmods.quantify.jvm;

public record JvmFunctionDefinition(
        String name,
        boolean isVisible,
        FunctionAddress address
) {}
