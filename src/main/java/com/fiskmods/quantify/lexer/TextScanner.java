package com.fiskmods.quantify.lexer;

import com.fiskmods.quantify.exception.QtfLexerException;
import com.fiskmods.quantify.lexer.token.Token;

import javax.annotation.Nullable;

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

    @Nullable
    public <T> ScannerPattern.MatchResult<T> peek(final ScannerPattern<T> pattern) throws QtfLexerException {
        return pattern.match(text, scanIndex);
    }

    @Nullable
    public <T> T next(final ScannerPattern<T> pattern) throws QtfLexerException {
        final ScannerPattern.MatchResult<T> result = peek(pattern);
        if (result == null) {
            return null;
        }
        advance(result);
        return result.match();
    }

    public String fullTrace() {
        return address(text, scanIndex) + trace(text, scanIndex);
    }

    public static String address(final String text, final int scanIndex) {
        String s = text.substring(0, scanIndex);
        int line = 1, i;

        while ((i = s.indexOf('\n')) != -1) {
            s = s.substring(i + 1);
            ++line;
        }
        return "line %s, column %s".formatted(line, s.length() + 1);
    }

    public static String trace(final String text, final int scanIndex) {
        String s = text;
        int i, index = scanIndex;

        if ((i = s.indexOf('\n', index)) > -1) {
            s = s.substring(0, i);
        }
        if ((i = s.lastIndexOf('\n')) > -1) {
            s = s.substring(i + 1);
            index -= i + 1;
        }

        final int start = Math.max(index - 64, 0);
        final String s1 = s.substring(start, Math.min(index + 64, s.length()));
        return '\n' + " ".repeat(64 - index + start)
                + s1 + '\n' + " ".repeat(63) + " ^";
    }
}
