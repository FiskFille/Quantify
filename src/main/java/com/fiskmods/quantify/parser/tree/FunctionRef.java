package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.FunctionAddress;

import java.util.List;

public record FunctionRef(
        FunctionAddress address,
        List<? extends Expression> args
) implements Expression {}
