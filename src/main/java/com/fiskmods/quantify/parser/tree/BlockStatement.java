package com.fiskmods.quantify.parser.tree;

import java.util.List;

public final class BlockStatement extends Statement {
    private final List<? extends Statement> statements;

    public BlockStatement(final List<? extends Statement> statements) {
        this.statements = statements;
    }

    public List<? extends Statement> statements() {
        return statements;
    }
}
