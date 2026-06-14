package com.fiskmods.quantify.lexer.token;

import com.fiskmods.quantify.exception.QtfParseException;
import org.jspecify.annotations.Nullable;

public record Token(TokenClass type, @Nullable Object value, Range range) {
    public record Range(int startIndex, int endIndex) {
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
