package com.fiskmods.quantify.jvm;

public interface JvmFunctionDefinition {
    String name();

    FunctionAddress address();

    JvmClassComposer define(String className);
}
