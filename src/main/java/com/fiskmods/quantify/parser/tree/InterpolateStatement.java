package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.VarAddress;
import org.jspecify.annotations.Nullable;

public record InterpolateStatement(
        Expression progress,
        @Nullable VarAddress substitution,
        Statement body
) implements Statement {}
