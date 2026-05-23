package com.fiskmods.quantify.lexer.token;

import com.fiskmods.quantify.exception.QtfParseException;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;

public interface TokenStream extends Iterator<Token> {
    /**
     * Clears the last peeked token, allowing any subsequent {@link TokenStream#peek()}
     * call to move on to a different token.
     */
    void clearPeekedToken();

    /**
     * Gets the next token in the stream without advancing to the next.
     *
     * @return the next token that will be returned by {@link TokenStream#next()},
     *          or <code>null</code> if no more tokens remain
     */
    Token peek();

    /**
     * Gets the last token that was consumed from the stream.
     *
     * @return the last consumed token, or <code>null</code> if no token has been consumed yet
     */
    @Nullable Token last();

    /**
     * Checks whether any tokens remain in the token stream.
     *
     * @return <code>true</code> if there are more tokens to be consumed
     */
    @Override
    boolean hasNext();

    /**
     * Checks whether any tokens remain in the token stream, within the specified
     * boundary. If none remain, the peeked token state is cleared.
     *
     * @param boundary the boundary within which to check for more tokens
     * @return <code>true</code> if there are more tokens
     */
    boolean hasNext(Boundary boundary);

    /**
     * Gets the next token in the token stream.
     *
     * @return the next token in the stream
     */
    @Override
    Token next();

    /**
     * Gets the next token in the stream, provided that it matches the expected
     * token class.
     *
     * @param expectedClass the token class to check for
     * @return the next token in the stream
     * @throws QtfParseException if the next token doesn't match
     */
    Token next(TokenClass expectedClass) throws QtfParseException;

    /**
     * Checks if the next token in the stream is of the specified token class.
     *
     * @param expectedClass the token class to check for
     * @return <code>true</code> if there are more tokens, and the next matches requirements
     */
    boolean isNext(TokenClass expectedClass);

    /**
     * Checks if the next token in the stream is of the specified token class,
     * with the specified value attached. <code>value</code> may be <code>null</code>.
     *
     * @param expectedClass the token class to check for
     * @param expectedValue the token value to check for, or <code>null</code>
     * @return <code>true</code> if there are more tokens, and the next matches requirements
     */
    boolean isNext(TokenClass expectedClass, Object expectedValue);

    /**
     * Skips past as many tokens in a row as needed, so long as they match
     * the given token class.
     *
     * @param tokenClass the token class to check for
     * @return <code>true</code> if any tokens were skipped
     */
    boolean skip(TokenClass tokenClass);

    enum Boundary {
        LINE {
            @Override
            public boolean isValidNextToken(final TokenClass tokenClass) {
                return true;
            }
        },
        EXPRESSION {
            @Override
            public boolean isValidNextToken(final TokenClass tokenClass) {
                return switch (tokenClass) {
                    case CLOSE_PARENTHESIS, CLOSE_BRACES, COMMA -> false;
                    default -> true;
                };
            }
        };

        public abstract boolean isValidNextToken(TokenClass tokenClass);
    }
}
