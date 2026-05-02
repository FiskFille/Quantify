package com.fiskmods.quantify;

import com.fiskmods.quantify.lexer.TextScanner;
import org.jspecify.annotations.Nullable;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import java.io.PrintWriter;
import java.util.Locale;

class LoggerImpl implements Logger {
    private static final int PADDING = 8;
    private static final String FORMAT = "%1$s:%2$d:%3$d: "
            + "%4$s: %5$s"
            + " at line %2$d, column %3$d%n";

    private final QtfSourceFile sourceFile;
    private final TextScanner scanner;
    private final PrintWriter writer;
    private final @Nullable DiagnosticListener<QtfSourceFile> diagnostics;

    LoggerImpl(final QtfSourceFile sourceFile, final TextScanner scanner, final PrintWriter writer, @Nullable final DiagnosticListener<QtfSourceFile> diagnostics) {
        this.sourceFile = sourceFile;
        this.scanner = scanner;
        this.writer = writer;
        this.diagnostics = diagnostics;
    }

    @Override
    public void logError(final String message) {
        logDiagnostic(new QtfError(sourceFile, scanner.getLocation(), message));
    }

    @Override
    public void logError(final String message, final int pos) {
        logDiagnostic(new QtfError(sourceFile, scanner.getLocation(pos), message));
    }

    public void logDiagnostic(final Diagnostic<QtfSourceFile> diagnostic) {
        if (diagnostics != null) {
            diagnostics.report(diagnostic);
        }

        final Locale locale = Locale.getDefault();
        final String label = getLabel(diagnostic.getKind());

        final String fileName = diagnostic.getSource().getName();
        final String message = diagnostic.getMessage(locale);

        final int line = (int) diagnostic.getLineNumber();
        final int col = (int) diagnostic.getColumnNumber();

        writer.printf(locale, FORMAT, fileName, line, col, label, message);

        final String lineText = scanner.substring((int) diagnostic.getStartPosition(), (int) diagnostic.getEndPosition());
        printTrace(lineText, col, writer);
        writer.flush();
    }

    private static void printTrace(final String line, final int pos, final PrintWriter writer) {
        final String indent = " ".repeat(PADDING);
        writer.print(indent);
        writer.println(line);
        writer.print(indent);

        for (int i = 0; i < pos - 1; i++) {
            writer.print(line.charAt(i) == '\t' ? "\t" : " ");
        }
        writer.println("^");
        writer.flush();
    }

    private static String getLabel(final Diagnostic.Kind kind) {
        return switch (kind) {
            case ERROR -> "error";
            case WARNING, MANDATORY_WARNING -> "warn";
            case NOTE -> "note";
            default -> "info";
        };
    }
}
