package com.fiskmods.quantify.lexer;

import com.fiskmods.quantify.exception.QtfLexerException;
import com.fiskmods.quantify.lexer.token.Token;
import org.jspecify.annotations.Nullable;

public class TextScanner {
    private final String text;

    private int scanIndex;
    private int scanLength;

    public TextScanner(final String text) {
        this.text = text;
    }

    public int getScanIndex() {
        return scanIndex;
    }

    public int getStartIndex() {
        return scanIndex - scanLength;
    }

    public Token.Range scanRange() {
        return new Token.Range(scanIndex - scanLength, scanIndex);
    }

    public void advance() {
        ++scanIndex;
        scanLength = 1;
    }

    public void advance(final ScannerPattern.MatchResult<?> result) {
        scanLength = result.length();
        scanIndex += scanLength;
    }

    public void skip(final int length) {
        scanLength = length;
        scanIndex += length;
    }

    /**
     * Expands the selection to include up to <code>length</code> more characters.
     * @param length the number of additional characters to include
     */
    public void expand(final int length) {
        scanLength += length;
        scanIndex += length;
    }

    public boolean hasNext() {
        return scanIndex < text.length();
    }

    public char nextChar() {
        return text.charAt(++scanIndex - 1);
    }

    public char peekChar() {
        return text.charAt(scanIndex);
    }

    public boolean tryConsume(final char expected) {
        if (peekChar() == expected) {
            expand(1);
            return true;
        } else {
            return false;
        }
    }

    public <T> ScannerPattern.@Nullable MatchResult<T> peek(final ScannerPattern<T> pattern) throws QtfLexerException {
        return pattern.match(text, scanIndex);
    }

    public <T> @Nullable T next(final ScannerPattern<T> pattern) throws QtfLexerException {
        final ScannerPattern.MatchResult<T> result = peek(pattern);
        if (result == null) {
            return null;
        }
        advance(result);
        return result.match();
    }

    public String text() {
        return text;
    }

    public String substring(final int start, final int end) {
        return text.substring(start, end);
    }

    public Location getLocation() {
        return getLocation(scanIndex);
    }

    public Location getLocation(final int index) {
        int start = 0;
        int line = 1;
        for (int i; (i = text.indexOf('\n', start, index)) != -1; line++) {
            start = i + 1;
        }

        int end = text.indexOf('\n', index);
        if (end < 0) {
            end = text.length();
        }

        final int column = index - start + 1;
        return new Location(start, end, line, column);
    }

    public record Location(int lineStart, int lineEnd, int line, int column) {}
}
