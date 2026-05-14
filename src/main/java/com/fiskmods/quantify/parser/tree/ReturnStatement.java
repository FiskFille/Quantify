package com.fiskmods.quantify.parser.tree;

public record ReturnStatement(
        Expression expression
) implements Statement {}
