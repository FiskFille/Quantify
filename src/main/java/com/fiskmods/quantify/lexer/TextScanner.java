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

    public Location getLocation() {
        return location(text, scanIndex);
    }

    public Trace createTrace() {
        return getLocation().createTrace(text);
    }

    public static Location location(final String text, final int index) {
        int start = 0;
        int line = 1;
        for (int i; (i = text.indexOf('\n', start, index)) != -1; line++) {
            start = i + 1;
        }

        final int column = index - start + 1;
        return new Location(start, line, column);
    }

    public record Location(int lineStart, int line, int column) {
        public Trace createTrace(final String text) {
            int lineEnd = text.indexOf('\n', lineStart + column - 1);
            if (lineEnd < 0) {
                lineEnd = text.length();
            }

            final String snippet = text.substring(lineStart, lineEnd);
            return new Trace(snippet, this);
        }
    }

    public record Trace(String snippet, Location location) {
        public String formattedString(final int padding) {
            final String tab = " ".repeat(padding);
            return tab + snippet + '\n' + tab + " ".repeat(location.column - 1) + '^';
        }
    }
}
