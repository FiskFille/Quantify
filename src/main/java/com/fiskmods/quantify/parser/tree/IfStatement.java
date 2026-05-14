package com.fiskmods.quantify.parser.tree;

import org.jspecify.annotations.Nullable;

public record IfStatement(
        Expression condition,
        Statement body,
        @Nullable Statement elseBody
) implements Statement {}
