package com.fiskmods.quantify.lexer.token;

import com.fiskmods.quantify.exception.QtfParseException;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.Objects;

public class IteratorTokenStream implements TokenStream {
    private final Iterator<Token> tokens;

    private @Nullable Token peekedToken, lastToken, eofToken;

    public IteratorTokenStream(final Iterator<Token> tokens) {
        this.tokens = tokens;
    }

    @Override
    public void clearPeekedToken() {
        peekedToken = null;
    }

    @Override
    public Token peek() {
        if (peekedToken != null) {
            return peekedToken;
        } else {
            return tokens.hasNext() ? peekedToken = tokens.next() : eof();
        }
    }

    @Override
    public @Nullable Token last() {
        return lastToken;
    }

    @Override
    public boolean hasNext() {
        return tokens.hasNext();
    }

    @Override
    public boolean hasNext(final Boundary boundary) {
        final Token peeked = peek();
        if (peeked.type() == TokenClass.TERMINATOR) {
            clearPeekedToken();
            return false;
        } else if (peeked.type() == TokenClass.EOF) {
            return false;
        }
        return boundary.isValidNextToken(peeked.type());
    }

    @Override
    public Token next() {
        if (peekedToken != null) {
            lastToken = peekedToken;
            peekedToken = null;
            return lastToken;
        } else {
            return tokens.hasNext() ? lastToken = tokens.next() : eof();
        }
    }

    @Override
    public Token next(final TokenClass expectedClass) throws QtfParseException {
        final Token next = next();
        if (next.type() == expectedClass) {
            return next;
        }
        throw unexpectedToken(next, expectedClass);
    }

    @Override
    public boolean isNext(final TokenClass expectedClass) {
        return peek().type() == expectedClass;
    }

    @Override
    public boolean isNext(final TokenClass expectedClass, final Object expectedValue) {
        final Token peeked = peek();
        return peeked.type() == expectedClass && Objects.equals(expectedValue, peeked.value());
    }

    @Override
    public boolean skip(final TokenClass tokenClass) {
        boolean flag = false;
        while (hasNext() && peek().type() == tokenClass) {
            clearPeekedToken();
            flag = true;
        }
        return flag;
    }

    private QtfParseException unexpectedToken(final Token found, final TokenClass expectedClass) {
        return new QtfParseException("Unexpected token '" + found + "'", "expected " + expectedClass, found.range());
    }

    private Token eof() {
        if (eofToken == null) {
            final int eofPos = lastToken != null ? lastToken.range().endIndex() : 0;
            eofToken = new Token(TokenClass.EOF, null, new Token.Range(eofPos, eofPos));
        }
        return eofToken;
    }
}
