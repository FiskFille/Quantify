package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.assignable.VarType;
import org.jspecify.annotations.Nullable;

public record VarDefinitionTree(
        Assignable target,
        VarType<?> type,
        @Nullable Expression initializer,
        boolean isPublic
) implements Statement {}
