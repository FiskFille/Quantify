package com.fiskmods.quantify.parser.tree;

public record ReturnStatement(
        Value value
) implements Statement {}
