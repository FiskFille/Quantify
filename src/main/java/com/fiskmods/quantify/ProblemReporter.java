package com.fiskmods.quantify;

import com.fiskmods.quantify.exception.QtfCompilerException;
import com.fiskmods.quantify.lexer.TextScanner;

import java.io.PrintStream;

public interface ProblemReporter {

    void report(Problem problem) throws QtfCompilerException;

    void flush() throws QtfCompilerException;

    default void report(final String message, final TextScanner scanner, final String fileName) throws QtfCompilerException {
        final TextScanner.Trace trace = scanner.createTrace();
        report(new Problem(message, trace, fileName));
    }

    default void report(final String message, final int startIndex, final String text, final String fileName) throws QtfCompilerException {
        final TextScanner.Location location = TextScanner.location(text, startIndex);
        final TextScanner.Trace trace = location.createTrace(text);

        report(new Problem(message, trace, fileName));
    }

    ProblemReporter EARLY_EXIT = new ProblemReporter() {
        @Override
        public void report(final Problem problem) throws QtfCompilerException {
            problem.print();
            throw new QtfCompilerException(problem.message);
        }

        @Override
        public void flush() {
        }
    };

    record Problem(String message, TextScanner.Trace trace, String fileName) {
        private static final String ERROR_FORMAT = "%1$s:%2$d:%3$d: error: %4$s"
                + " at line %2$d, column %3$d"
                + "%n%5$s%n";

        public void print(final PrintStream s) {
            final String formattedTrace = trace.formattedString(4);

            final TextScanner.Location location = trace.location();
            final int line = location.line();
            final int column = location.column();

            s.printf(ERROR_FORMAT, fileName, line, column, message, formattedTrace);
        }

        public void print() {
            print(System.err);
        }
    }
}
