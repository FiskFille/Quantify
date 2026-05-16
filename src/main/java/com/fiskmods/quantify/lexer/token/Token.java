package com.fiskmods.quantify.lexer.token;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.parser.SyntaxContext;
import org.jspecify.annotations.Nullable;

public record Token(TokenClass type, @Nullable Object value, Range range) {
    public Token(final TokenClass type, @Nullable final Object value, final int startIndex, final int endIndex) {
        this(type, value, new Range(startIndex, endIndex));
    }

    public record Range(int startIndex, int endIndex) {
        public static final Range ZERO = new Range(0, 0);

        public Range union(final Range range) {
            return new Range(Math.min(startIndex, range.startIndex), Math.max(endIndex, range.endIndex));
        }
    }

    public String getString() throws QtfParseException {
        if (value instanceof String) {
            return (String) value;
        }
        throw QtfParseException.internal("token '%s' is not a string".formatted(this), range);
    }

    public Operator getOperator() throws QtfParseException {
        if (value instanceof Operator) {
            return (Operator) value;
        }
        throw QtfParseException.internal("token '%s' is not an operator".formatted(this), range);
    }

    public @Nullable Operator getAssignmentOperator(final SyntaxContext context, final boolean isDefinition) throws QtfParseException {
        if (value == null) {
            return null;
        }
        final Operator op = getOperator();
        if (isDefinition) {
            throw QtfParseException.error("definitions can't use assignment operators", range);
        }
        if ((op == Operator.LERP || op == Operator.LERP_ROT) && context.scope().getLerpProgress() == null) {
            throw QtfParseException.error("interpolation assignments can only be used inside" + " interpolate blocks", range);
        }
        return op;
    }

    public Number getNumber() throws QtfParseException {
        if (value instanceof Number) {
            return (Number) value;
        }
        throw QtfParseException.internal("token '%s' is not a number".formatted(this), range);
    }

    @Override
    public String toString() {
        return value == null ? type.toString() : type + " " + value;
    }
}
