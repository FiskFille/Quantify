package com.fiskmods.quantify;

import com.fiskmods.quantify.exception.QtfCompilerException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.DynamicClassLoader;
import com.fiskmods.quantify.jvm.JvmClassComposer;
import com.fiskmods.quantify.jvm.JvmCompiler;
import com.fiskmods.quantify.jvm.JvmRunnable;
import com.fiskmods.quantify.lexer.QtfLexer;
import com.fiskmods.quantify.lexer.TextScanner;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.library.LibraryMap;
import com.fiskmods.quantify.member.QtfListener;
import com.fiskmods.quantify.member.QtfMemory;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxTree;
import org.jspecify.annotations.Nullable;

import javax.tools.DiagnosticListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class QtfCompiler {
    public static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("com.fiskmods.quantify.Debug", "false"));

    private final NameProvider nameProvider = new NameProvider("com/fiskmods/quantify/dynamic/Compiled");

    private final Supplier<DynamicClassLoader> classLoaderFactory;
    private @Nullable DynamicClassLoader classLoader;

    public QtfCompiler(final Supplier<DynamicClassLoader> classLoaderFactory) {
        this.classLoaderFactory = classLoaderFactory;
    }

    public QtfCompiler() {
        this(DynamicClassLoader::new);
    }

    public QtfScript compile(
            final QtfSourceFile sourceFile,
            final LibraryMap libraries,
            final @Nullable QtfListener listener,
            final @Nullable DiagnosticListener<QtfSourceFile> diagnostics) throws QtfCompilerException {

        final TextScanner scanner = readFile(sourceFile);
        final Logger logger = new LoggerImpl(sourceFile, scanner, new PrintWriter(System.err), diagnostics);

        try {
            final SyntaxContext context = new SyntaxContext(libraries);

            final List<Token> tokens = tokenize(scanner, logger);
            final SyntaxTree syntaxTree = parse(tokens.iterator(), context, logger);

            return compile(syntaxTree, context, listener);
        } catch (final QtfCompilerException e) {
            throw QtfCompilerException.attachSource(e, sourceFile);
        }
    }

    public QtfScript compile(final SyntaxTree tree, final SyntaxContext context, final @Nullable QtfListener listener) throws QtfCompilerException {
        try {
            if (classLoader == null) {
                classLoader = classLoaderFactory.get();
            }
            final String className = nameProvider.next();
            final QtfMemory memory = context.createMemory(listener);
            final JvmClassComposer composer = context.createClassComposer(className);
            final JvmRunnable runnable = JvmCompiler.compile(tree, composer, className, classLoader);
            return new QtfScript(runnable, memory, context.getInputs());
        } catch (final Exception e) {
            throw new QtfCompilerException(e);
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

    private List<Token> tokenize(final TextScanner scanner, final Logger logger) throws QtfCompilerException {
        final List<Token> tokens = new ArrayList<>();

        final QtfLexer lexer = new QtfLexer(scanner, logger);
        if (lexer.read(tokens::add)) {
            if (QtfCompiler.DEBUG) {
                System.out.println(tokens);
            }
            return tokens;
        }

        throw new QtfCompilerException("Invalid source");
    }

    private SyntaxTree parse(final Iterator<Token> tokens, final SyntaxContext context, final Logger logger) throws QtfCompilerException {
        try {
            final QtfParser parser = new QtfParser(tokens, context);
            return parser.parse(false);
        } catch (final QtfParseException e) {
            logger.logError(e.getMessage(), e.getRange().startIndex());
            throw new QtfCompilerException("Syntax error");
        }
    }

    /**
     * Triggers garbage collection on the ClassLoader so that any scripts
     * loaded from it can be released from memory.
     */
    public void flush() {
        classLoader = null;
        System.gc();
    }

    private record NameProvider(String path, AtomicInteger id) {
        public NameProvider(final String path) {
            this(path, new AtomicInteger());
        }

        public String next() {
            return path + id.getAndIncrement();
        }
    }
}
