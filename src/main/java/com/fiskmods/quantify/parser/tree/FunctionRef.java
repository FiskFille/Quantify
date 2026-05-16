package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.lexer.token.Token;

import java.util.List;

public final class FunctionRef extends Expression {
    private final FunctionAddress address;
    private final List<? extends Expression> args;

    private FunctionRef(final FunctionAddress address, final List<? extends Expression> args) {
        this.address = address;
        this.args = args;
    }

    public static FunctionRef of(final FunctionAddress address, final List<? extends Expression> args, final Token.Range range) {
        final FunctionRef func = new FunctionRef(address, args);
        func.range = range;
        return func;
    }

    public static FunctionRef of(final FunctionAddress address, final Expression arg) {
        return of(address, List.of(arg), arg.range());
    }

    public FunctionAddress address() {
        return address;
    }

    public List<? extends Expression> args() {
        return args;
    }
}
