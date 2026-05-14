package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.lexer.token.Operator;
import org.jspecify.annotations.Nullable;

public final class Assignment extends Tree implements Statement {
    private final Assignable target;
    private final Expression value;
    private final @Nullable Operator op;

    public Assignment(final Assignable target, final Expression value, final @Nullable Operator op) {
        this.target = target;
        this.value = value;
        this.op = op;
    }

    public Assignable target() {
        return target;
    }

    public Expression value() {
        return value;
    }

    public @Nullable Operator op() {
        return op;
    }
}
