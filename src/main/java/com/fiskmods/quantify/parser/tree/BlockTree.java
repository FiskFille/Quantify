package com.fiskmods.quantify.parser.tree;

import java.util.List;

public record BlockTree(
        List<? extends Tree> statements
) implements Tree {}
