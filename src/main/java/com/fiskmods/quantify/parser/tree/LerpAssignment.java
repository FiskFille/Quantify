package com.fiskmods.quantify.parser.tree;

public record LerpAssignment(
        Assignable target,
        Expression value,
        Expression progress,
        boolean rotational
) implements Statement {}
