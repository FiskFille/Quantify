package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.VarAddress;

import java.util.List;

public record VariableList(
        List<? extends VarAddress> addresses
) implements Assignable {}
