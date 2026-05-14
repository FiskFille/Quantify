package com.fiskmods.quantify.parser.tree;

import org.jspecify.annotations.Nullable;

public record IfStatement(
        Expression condition,
        Tree body,
        @Nullable Tree elseBody
) implements Statement {}
