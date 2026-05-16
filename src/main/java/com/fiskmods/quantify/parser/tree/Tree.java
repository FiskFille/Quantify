package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.lexer.token.Token;
import org.jspecify.annotations.Nullable;

public sealed abstract class Tree permits Expression, Statement {
    protected Token.@Nullable Range range;

    public Token.@Nullable Range range() {
        return range;
    }
}
