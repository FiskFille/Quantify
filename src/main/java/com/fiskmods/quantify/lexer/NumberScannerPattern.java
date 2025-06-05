package com.fiskmods.quantify.lexer;

import javax.annotation.Nullable;

public class NumberScannerPattern implements ScannerPattern<Number> {
    @Nullable
    @Override
    public MatchResult<Number> match(final String text, final int startIndex) {
        char c = text.charAt(startIndex);
        if (!Character.isDigit(c)) {
            return null;
        }

        boolean decimal = false;
        int endIndex = startIndex + 1;

        for (; endIndex < text.length(); ++endIndex) {
            c = text.charAt(endIndex);

            if (!Character.isDigit(c)) {
                if (c == '.' && !decimal && endIndex + 1 < text.length()
                        && Character.isDigit(text.charAt(endIndex + 1))) {
                    decimal = true;
                    ++endIndex;
                } else {
                    break;
                }
            }
        }

        final String match = text.substring(startIndex, endIndex);
        final int length = match.length();
        if (decimal) {
            return new MatchResult<>(Double.parseDouble(match), length);
        }
        return new MatchResult<>(Integer.parseInt(match), length);
    }
}
