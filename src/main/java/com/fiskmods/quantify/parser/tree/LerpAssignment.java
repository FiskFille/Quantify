package com.fiskmods.quantify.parser.tree;

public record LerpAssignment(
        Assignable target,
        Value value,
        Value progress,
        boolean rotational
) implements Statement {}
