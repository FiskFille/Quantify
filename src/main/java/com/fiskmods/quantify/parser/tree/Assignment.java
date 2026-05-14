package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.lexer.token.Operator;
import org.jspecify.annotations.Nullable;

public record Assignment(
        Assignable target,
        Expression value,
        @Nullable Operator op
) implements Statement {}
