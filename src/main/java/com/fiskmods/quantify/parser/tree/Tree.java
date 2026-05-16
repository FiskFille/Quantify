package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.lexer.token.Token;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public sealed abstract class Tree permits Expression, Statement {
    protected Token.@Nullable Range range;

    public Token.Range range() {
        return Objects.requireNonNull(range, "range");
    }
}
