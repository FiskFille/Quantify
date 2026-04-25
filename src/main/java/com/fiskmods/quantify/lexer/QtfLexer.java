package com.fiskmods.quantify.lexer;

import com.fiskmods.quantify.exception.QtfLexerException;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

import static com.fiskmods.quantify.lexer.token.Operator.*;
import static com.fiskmods.quantify.lexer.token.TokenClass.*;

public class QtfLexer {
    private final TextScanner scanner;

    public QtfLexer(final TextScanner scanner) {
        this.scanner = scanner;
    }

    public QtfLexer(final String text) {
        this(new TextScanner(text));
    }

    public void read(final Consumer<Token> tokenConsumer) throws QtfLexerException {
        try {
            readUnsafe(new TokenGenerator(scanner, tokenConsumer));
        } catch (final QtfLexerException e) {
            throw new QtfLexerException(e.getMessage() + " at " + scanner.fullTrace(), e);
        }
    }

    private void readUnsafe(final TokenGenerator tokens) throws QtfLexerException {
        String result;
        char c;

        while (scanner.hasNext()) {
            c = scanner.peekChar();
            if (Character.isWhitespace(c)) {
                scanner.advance();
                if (isTerminator(c)) {
                    tokens.insert(TERMINATOR);
                }
                continue;
            }

            // Skip past comments
            if (scanner.next(ScannerPattern.COMMENT) != null) {
                continue;
            }

            singleChar: {
                scanner.advance();
                switch (c) {
                    case '+' -> readOperator(tokens, ADD);
                    case '-' -> {
                        switch (scanner.peekChar()) {
                            case '=' -> {
                                scanner.expand(1);
                                tokens.insert(ASSIGNMENT, SUB);
                            }
                            case '>' -> {
                                scanner.expand(1);
                                tokens.insert(ASSIGNMENT, LERP);
                            }
                            case '\'' -> {
                                scanner.expand(1);
                                if (scanner.tryConsume('>')) {
                                    tokens.insert(ASSIGNMENT, LERP_ROT);
                                } else {
                                    scanner.skip(-2);
                                    scanner.advance();
                                    tokens.insert(OPERATOR, SUB);
                                }
                            }
                            default -> tokens.insert(OPERATOR, SUB);
                        }
                    }
                    case '*' -> readOperator(tokens, MUL);
                    case '/' -> readOperator(tokens, DIV);
                    case '^' -> readOperator(tokens, POW);
                    case '%' -> readOperator(tokens, MOD);
                    case '&' -> {
                        if (readDoubleOperator(tokens, '&', AND)) {
                            break singleChar;
                        }
                    }
                    case '|' -> {
                        if (readDoubleOperator(tokens, '|', OR)) {
                            break singleChar;
                        }
                    }

                    case '<' -> readEqualityOperator(tokens, LT, LEQ);
                    case '>' -> readEqualityOperator(tokens, GT, GEQ);
                    case '!' -> {
                        if (scanner.tryConsume('=')) {
                            tokens.insert(OPERATOR, NEQ);
                        } else {
                            tokens.insert(NOT);
                        }
                    }
                    case '=' -> {
                        if (scanner.tryConsume('=')) {
                            tokens.insert(OPERATOR, EQ);
                        } else {
                            tokens.insert(ASSIGNMENT);
                        }
                    }

                    case '.' -> tokens.insert(DOT);
                    case ':' -> tokens.insert(COLON);
                    case ',' -> tokens.insert(COMMA);
                    case '(' -> tokens.insert(OPEN_PARENTHESIS);
                    case ')' -> tokens.insert(CLOSE_PARENTHESIS);
                    case '{' -> tokens.insert(OPEN_BRACES);
                    case '}' -> tokens.insert(CLOSE_BRACES);
                    case '[' -> tokens.insert(OPEN_BRACKETS);
                    case ']' -> tokens.insert(CLOSE_BRACKETS);
                    case '\'' -> tokens.insert(DEGREES);
                    default -> {
                        // Backtrack
                        scanner.skip(-1);
                        break singleChar;
                    }
                }
                continue;
            }

            final Number numResult;
            if ((numResult = scanner.next(ScannerPattern.NUMBER)) != null) {
                tokens.insert(NUM_LITERAL, numResult);
                continue;
            }
            if ((result = scanner.next(ScannerPattern.STRING)) != null) {
                tokens.insert(STR_LITERAL, result);
                continue;
            }
            if ((result = scanner.next(ScannerPattern.IDENTIFIER)) != null) {
                switch (result) {
                    // Syntax keywords
                    case Keywords.VAR -> tokens.insert(VAR);
                    case Keywords.CONST -> tokens.insert(CONST);
                    case Keywords.FUNC -> tokens.insert(FUNC);
                    case Keywords.IMPORT -> tokens.insert(IMPORT);
                    case Keywords.INPUT -> tokens.insert(INPUT);
                    case Keywords.PUBLIC -> tokens.insert(PUBLIC);

                    // Control keywords
                    case Keywords.IF -> tokens.insert(IF);
                    case Keywords.ELSE -> tokens.insert(ELSE);
                    case Keywords.INTERPOLATE -> tokens.insert(INTERPOLATE);
                    case Keywords.NAMESPACE -> tokens.insert(NAMESPACE);
                    case Keywords.RETURN -> tokens.insert(RETURN);

                    // Constants
                    case "pi" -> tokens.insert(NUM_LITERAL, Math.PI);
                    case "e" -> tokens.insert(NUM_LITERAL, Math.E);
                    case "NaN" -> tokens.insert(NUM_LITERAL, Double.NaN);
                    case "Inf" -> tokens.insert(NUM_LITERAL, Double.POSITIVE_INFINITY);
                    case "true" -> tokens.insert(NUM_LITERAL, 1);
                    case "false" -> tokens.insert(NUM_LITERAL, 0);

                    default -> tokens.insert(IDENTIFIER, result);
                }
                continue;
            }

            throw new QtfLexerException("Unknown symbol '%s'".formatted(c));
        }
    }

    private void readOperator(final TokenGenerator tokens, final Operator operator) {
        if (scanner.tryConsume('=')) {
            tokens.insert(ASSIGNMENT, operator);
        } else {
            tokens.insert(OPERATOR, operator);
        }
    }

    private boolean readDoubleOperator(final TokenGenerator tokens, final char c, final Operator operator) {
        if (scanner.tryConsume(c)) {
            readOperator(tokens, operator);
            return false;
        }
        // Backtrack
        scanner.skip(-1);
        return true;
    }

    private void readEqualityOperator(final TokenGenerator tokens, final Operator operator1, final Operator operator2) {
        tokens.insert(OPERATOR, scanner.tryConsume('=') ? operator2 : operator1);
    }

    public static boolean isNonAlphanumeric(final char c) {
        return c != '_' && (c < 'a' || c > 'z') && (c < 'A' || c > 'Z') && (c < '0' || c > '9');
    }

    public static boolean isTerminator(final char c) {
        return c == '\n' || c == '\r';
    }

    private record TokenGenerator(TextScanner scanner, Consumer<Token> tokens) {
        void insert(final TokenClass tokenClass, final @Nullable Object value) {
            tokens.accept(new Token(tokenClass, value, scanner.scanRange()));
        }

        void insert(final TokenClass tokenClass) {
            insert(tokenClass, null);
        }
    }
}
