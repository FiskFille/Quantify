package com.fiskmods.quantify.parser.tree;

import java.util.List;

public record BlockStatement(
        List<? extends Statement> statements
) implements Statement {}
