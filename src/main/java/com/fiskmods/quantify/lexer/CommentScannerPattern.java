package com.fiskmods.quantify.lexer;

import org.jspecify.annotations.Nullable;

public class CommentScannerPattern implements ScannerPattern<String> {
    @Override
    public @Nullable MatchResult<String> match(final String text, final int startIndex) {
        return switch (text.charAt(startIndex)) {
            case '#' -> matchLine(text, startIndex);
            case '/' -> matchBlock(text, startIndex);
            default -> null;
        };
    }

    private MatchResult<String> matchLine(final String text, final int startIndex) {
        int endIndex = startIndex + 1;
        while (endIndex < text.length() && !QtfLexer.isTerminator(text.charAt(endIndex))) {
            ++endIndex;
        }
        return MatchResult.string(text.substring(startIndex, endIndex));
    }

    private @Nullable MatchResult<String> matchBlock(final String text, final int startIndex) {
        if (startIndex + 1 >= text.length() || text.charAt(startIndex + 1) != '#') {
            return null;
        }

        int endIndex = startIndex + 2;
        char c = '#', c1;
        for (; endIndex < text.length(); ++endIndex) {
            c1 = c;
            if ((c = text.charAt(endIndex)) == '/' && c1 == '#') {
                ++endIndex;
                break;
            }
        }
        return MatchResult.string(text.substring(startIndex, endIndex));
    }
}
