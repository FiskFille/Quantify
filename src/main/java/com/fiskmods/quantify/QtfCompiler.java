package com.fiskmods.quantify;

import com.fiskmods.quantify.exception.QtfCompilerException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.JvmCompiler;
import com.fiskmods.quantify.lexer.QtfLexer;
import com.fiskmods.quantify.lexer.TextScanner;
import com.fiskmods.quantify.lexer.token.IteratorTokenStream;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenStream;
import com.fiskmods.quantify.library.LibraryMap;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.tree.Tree;
import org.jspecify.annotations.Nullable;

import javax.tools.DiagnosticListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class QtfCompiler {
    public static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("com.fiskmods.quantify.Debug", "false"));

    private final JvmCompiler classCompiler;

    public QtfCompiler(final @Nullable ClassLoader fallbackClassLoader) {
        final AtomicInteger count = new AtomicInteger();
        this.classCompiler = new JvmCompiler(fallbackClassLoader,
                () -> "com/fiskmods/quantify/dynamic/Compiled" + count.getAndIncrement()
        );
    }

    public QtfCompiler() {
        this(null);
    }

    public QtfCompilationUnit compile(
            final QtfSourceFile sourceFile,
            final LibraryMap libraries,
            final @Nullable DiagnosticListener<QtfSourceFile> diagnostics) throws QtfCompilerException {

        final TextScanner scanner = readFile(sourceFile);
        final Logger logger = new LoggerImpl(sourceFile, scanner, new PrintWriter(System.err), diagnostics);

        try {
            final SyntaxContext context = new SyntaxContext(libraries);

            final TokenStream tokens = tokenize(scanner, logger);
            final var trees = parse(tokens, context, logger);

            final Class<?> c = classCompiler.compile(trees);
            return new QtfCompilationUnit(c,
                    context.getInputs(),
                    context.getOutputs(),
                    context.getFunctions()
            );
        } catch (final QtfCompilerException e) {
            throw QtfCompilerException.attachSource(e, sourceFile);
        }
    }

    private TextScanner readFile(final QtfSourceFile sourceFile) throws QtfCompilerException {
        try {
            final String text = sourceFile.getCharContent(false).toString();
            return new TextScanner(text);
        } catch (final IOException e) {
            throw new QtfCompilerException(e, sourceFile);
        }
    }

    private TokenStream tokenize(final TextScanner scanner, final Logger logger) throws QtfCompilerException {
        final List<Token> tokens = new ArrayList<>();

        final QtfLexer lexer = new QtfLexer(scanner, logger);
        if (lexer.read(tokens::add)) {
            if (QtfCompiler.DEBUG) {
                System.out.println(tokens);
            }
            return IteratorTokenStream.of(tokens.iterator());
        }

        throw new QtfCompilerException("Invalid source");
    }

    private List<? extends Tree> parse(final TokenStream tokens, final SyntaxContext context, final Logger logger) throws QtfCompilerException {
        try {
            final QtfParser parser = new QtfParser(tokens, context);
            return parser.parse(false);
        } catch (final QtfParseException e) {
            logger.logError(e.getMessage(), e.getRange().startIndex());
            throw new QtfCompilerException("Syntax error");
        }
    }
}
