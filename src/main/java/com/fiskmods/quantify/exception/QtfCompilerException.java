package com.fiskmods.quantify.exception;

import com.fiskmods.quantify.QtfCompiler;
import com.fiskmods.quantify.lexer.TextScanner;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.parser.QtfParser;

public class QtfCompilerException extends QtfException {
    public QtfCompilerException(Throwable cause) {
        super(cause);
    }

    public QtfCompilerException(String message) {
        super(message);
    }

    public QtfCompilerException(String message, Throwable cause) {
        super(message, cause);
    }

    public static QtfCompilerException handle(QtfLexerException cause) {
        return new QtfCompilerException(cause.getMessage());
    }

    public static QtfCompilerException handle(QtfParser parser, QtfParseException cause, String text) {
        String message = cause.getMessage();
        String reason = cause.getReason();
        Token.Range location = cause.getLocation(parser);

        if (location != null) {
            message += " at " + TextScanner.address(text, location.startIndex());
        }
        if (reason != null) {
            message += " - " + reason;
        }
        if (location != null) {
            message += TextScanner.trace(text, location.startIndex());
        }
        return QtfCompiler.DEBUG ? new QtfCompilerException(message, cause)
                : new QtfCompilerException(message);
    }
}
