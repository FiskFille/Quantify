package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.VarAddress;
import org.jspecify.annotations.Nullable;

public record InterpolateStatement(
        Value progress,
        @Nullable VarAddress substitution,
        Tree body
) implements Statement {}
