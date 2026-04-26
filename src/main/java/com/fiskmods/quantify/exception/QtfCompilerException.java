package com.fiskmods.quantify.exception;

import com.fiskmods.quantify.QtfCompiler;
import com.fiskmods.quantify.lexer.TextScanner;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.parser.QtfParser;

public class QtfCompilerException extends QtfException {
    public static final int TRACE_PADDING = 64;

    public QtfCompilerException(final Throwable cause) {
        super(cause);
    }

    private QtfCompilerException(final String message) {
        super(message);
    }

    private QtfCompilerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public static QtfCompilerException create(final String message, final Throwable cause) {
        return QtfCompiler.DEBUG ? new QtfCompilerException(message, cause) : new QtfCompilerException(message);
    }

    public static QtfCompilerException handle(final QtfLexerException cause, final String text) {
        final TextScanner.Location location = cause.location();
        if (location == null) {
            return create(cause.getMessage(), cause);
        }

        final TextScanner.Trace trace = location.createTrace(text);
        final String message = cause.getMessage()
                + " at " + location.formattedString()
                + '\n' + trace.formattedString(TRACE_PADDING);

        return create(message, cause);
    }

    public static QtfCompilerException handle(final QtfParser parser, final QtfParseException cause, final String text) {
        final String reason = cause.getReason();
        final Token.Range range = cause.getLocation(parser);

        String message = cause.getMessage();
        TextScanner.Trace trace = null;

        if (range != null) {
            final TextScanner.Location location = TextScanner.location(text, range.startIndex());
            trace = location.createTrace(text);
            message += " at " + location.formattedString();
        }
        if (reason != null) {
            message += " - " + reason;
        }
        if (trace != null) {
            message += '\n' + trace.formattedString(TRACE_PADDING);
        }

        return create(message, cause);
    }
}
