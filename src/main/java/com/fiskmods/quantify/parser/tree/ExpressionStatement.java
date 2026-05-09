package com.fiskmods.quantify.parser.tree;

public record ExpressionStatement(
        Expression expression
) implements Statement {}
