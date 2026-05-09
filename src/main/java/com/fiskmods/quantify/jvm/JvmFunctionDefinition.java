package com.fiskmods.quantify.jvm;

public interface JvmFunctionDefinition {
    String name();

    boolean isVisible();

    FunctionAddress address();
}
