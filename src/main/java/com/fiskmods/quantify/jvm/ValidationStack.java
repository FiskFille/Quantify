package com.fiskmods.quantify.jvm;

import com.fiskmods.quantify.Logger;
import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.lexer.token.Token;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Type;

public class ValidationStack {
    private final Logger logger;

    private @Nullable ExpressionValue value;
    boolean error;

    public ValidationStack(final Logger logger) {
        this.logger = logger;
    }

    public @Nullable ExpressionValue peek() {
        return value;
    }

    public void push(final @Nullable ExpressionValue value) {
        if (this.value != null) {
            throw new IllegalStateException("expected validation stack to be empty");
        }
        this.value = value;
    }

    public ExpressionValue pop() {
        return pop((Type) null);
    }

    public ExpressionValue pop(final @Nullable Type expectedType) {
        if (value == null) {
            throw new IllegalStateException("expected expression value");
        }

        final ExpressionValue head = value;
        value = null;
        try {
            head.validate(expectedType);
        } catch (final QtfException e) {
            handle(e, head.expression().range());
        }
        return head;
    }

    public void pop(final VarType<?> expectedType) {
        pop(expectedType.internal());
    }

    public void handle(final QtfException e, final Token.Range range) {
        logger.logError(e.getMessage(), range.startIndex());
        error = true;
    }
}
