package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.lexer.token.Operator;

import java.util.List;

public final class CompoundAssignment extends Statement {
    private final List<VarRef> targets;
    private final Expression value;
    private final Operator op;

    CompoundAssignment(final List<VarRef> targets, final Expression value, final Operator op) {
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

    public Operator op() {
        return op;
    }
}
