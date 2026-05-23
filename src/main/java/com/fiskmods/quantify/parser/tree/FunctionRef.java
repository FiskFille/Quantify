package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.lexer.token.Token;

import java.util.List;

public final class FunctionRef extends Expression {
    private final Expression selector;
    private final FunctionAddress address;
    private final List<? extends Expression> args;

    private FunctionRef(final Expression selector, final FunctionAddress address, final List<? extends Expression> args) {
        this.selector = selector;
        this.address = address;
        this.args = args;
    }

    public static FunctionRef of(final Expression selector, final FunctionAddress address, final List<? extends Expression> args, final Token.Range range) {
        final FunctionRef func = new FunctionRef(selector, address, args);
        func.range = range;
        return func;
    }

    public static FunctionRef of(final Expression selector, final FunctionAddress address, final Expression arg) {
        return of(selector, address, List.of(arg), arg.range());
    }

    public Expression selector() {
        return selector;
    }

    public FunctionAddress address() {
        return address;
    }

    public List<? extends Expression> args() {
        return args;
    }
}
