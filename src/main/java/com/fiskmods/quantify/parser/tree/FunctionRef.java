package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.lexer.token.Token;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class FunctionRef extends Expression {
    private final Expression selector;
    private final List<? extends Expression> args;

    public @Nullable FunctionAddress address;

    private FunctionRef(final Expression selector, final List<? extends Expression> args) {
        this.selector = selector;
        this.args = args;
    }

    public static FunctionRef of(final Expression selector, final List<? extends Expression> args, final Token.Range range) {
        final FunctionRef func = new FunctionRef(selector, args);
        func.range = range;
        return func;
    }

    public Expression selector() {
        return selector;
    }

    public List<? extends Expression> args() {
        return args;
    }
}
