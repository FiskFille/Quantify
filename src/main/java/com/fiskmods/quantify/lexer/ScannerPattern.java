package com.fiskmods.quantify.lexer;

import com.fiskmods.quantify.exception.QtfLexerException;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

@FunctionalInterface
public interface ScannerPattern<T> {
    @Nullable MatchResult<T> match(String text, int startIndex) throws QtfLexerException;

    record MatchResult<T>(T match, int length) {
        public static MatchResult<String> string(final String match) {
            return new MatchResult<>(match, match.length());
        }
    }

    ScannerPattern<String> IDENTIFIER = (text, startIndex) -> {
        char c = text.charAt(startIndex);
        // Cannot begin with a digit
        if (Character.isDigit(c) || QtfLexer.isNonAlphanumeric(c)) {
            return null;
        }
        int l = startIndex;
        while (++l < text.length()) {
            if (QtfLexer.isNonAlphanumeric(text.charAt(l))) {
                break;
            }
        }
        return MatchResult.string(text.substring(startIndex, l));
    };

    ScannerPattern<String> TOKEN = (text, startIndex) -> {
        char c = text.charAt(startIndex);
        if (QtfLexer.isNonAlphanumeric(c)) {
            return new MatchResult<>(String.valueOf(c), 1);
        }
        int l = startIndex;
        while (++l < text.length()) {
            if (QtfLexer.isNonAlphanumeric(text.charAt(l))) {
                break;
            }
        }
        return MatchResult.string(text.substring(startIndex, l));
    };

    //ScannerPattern INTEGER = phrase(Character::isDigit);
    ScannerPattern<Number> NUMBER = new NumberScannerPattern();
    ScannerPattern<String> STRING = new StringScannerPattern();
    ScannerPattern<String> COMMENT = new CommentScannerPattern();

    static ScannerPattern<String> phrase(final Predicate<Character> allowedChar) {
        return (text, startIndex) -> {
            int l = startIndex;
            while (l < text.length()) {
                if (!allowedChar.test(text.charAt(l))) {
                    break;
                }
                ++l;
            }
            if (startIndex == l) {
                return null;
            }
            return MatchResult.string(text.substring(startIndex, l));
        };
    }
}
