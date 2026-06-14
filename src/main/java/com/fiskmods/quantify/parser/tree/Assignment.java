package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.lexer.token.Operator;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class Assignment extends Statement {
    private final List<VarRef> targets;
    private final Expression value;
    private final @Nullable Operator op;

    Assignment(final List<VarRef> targets, final Expression value, final @Nullable Operator op) {
        this.targets = targets;
        this.value = value;
        this.op = op;
    }

    public List<VarRef> targets() {
        return targets;
    }

    public Expression value() {
        return value;
    }

    public @Nullable Operator op() {
        return op;
    }
}
