package com.fiskmods.quantify.parser.tree;

public final class ImportStatement extends Statement {
    private final String name;
    private final String key;

    ImportStatement(final String name, final String key) {
        this.name = name;
        this.key = key;
    }

    public String name() {
        return name;
    }

    public String key() {
        return key;
    }
}
