package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.library.QtfLibrary;

public final class ImportStatement extends Statement {
    private final Identifier name;
    private final String key;
    private final QtfLibrary library;

    ImportStatement(final Identifier name, final String key, final QtfLibrary library) {
        this.name = name;
        this.key = key;
        this.library = library;
    }

    public Identifier name() {
        return name;
    }

    public String key() {
        return key;
    }

    public QtfLibrary library() {
        return library;
    }
}
