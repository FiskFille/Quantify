package com.fiskmods.quantify;

import com.fiskmods.quantify.exception.QtfCompilerException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.DynamicClassLoader;
import com.fiskmods.quantify.jvm.JvmClassComposer;
import com.fiskmods.quantify.jvm.JvmCompiler;
import com.fiskmods.quantify.jvm.JvmRunnable;
import com.fiskmods.quantify.lexer.QtfLexer;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.library.QtfLibrary;
import com.fiskmods.quantify.member.QtfListener;
import com.fiskmods.quantify.member.QtfMemory;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxTree;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class QtfCompiler {
    public static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("com.fiskmods.quantify.Debug", "false"));

    private final NameProvider nameProvider = new NameProvider("com/fiskmods/quantify/dynamic/Compiled");
    private final Map<String, QtfLibrary> libraries = new HashMap<>();

    private final Supplier<DynamicClassLoader> classLoaderFactory;
    private @Nullable DynamicClassLoader classLoader;

    public QtfCompiler(final Supplier<DynamicClassLoader> classLoaderFactory) {
        this.classLoaderFactory = classLoaderFactory;
    }

    public QtfCompiler() {
        this(DynamicClassLoader::new);
    }

    public QtfCompiler addLibrary(final QtfLibrary library) {
        libraries.put(library.key(), library);
        return this;
    }

    public @Nullable QtfLibrary getLibrary(final String key) {
        return libraries.get(key);
    }

    public int libraries() {
        return libraries.size();
    }

    public QtfScript compile(final String fileName, final String text, final QtfListener listener, final ProblemReporter problems) throws QtfCompilerException {
        final List<Token> tokens = new ArrayList<>();
        final QtfLexer lexer = new QtfLexer(fileName, text, problems);
        lexer.read(tokens::add);
        if (QtfCompiler.DEBUG) {
            System.out.println(tokens);
        }

        problems.flush();

        final SyntaxContext context = new SyntaxContext(this);
        final SyntaxTree syntaxTree = new SyntaxTree(context);
        final QtfParser parser = new QtfParser(tokens.iterator(), context);

        try {
            parser.parse(syntaxTree, false);
        } catch (final QtfParseException e) {
            if (QtfCompiler.DEBUG) {
                e.printStackTrace();
            }
            final int startIndex = e.getStartIndex(parser);
            problems.report(e.getMessage(), startIndex, text, fileName);
        }

        problems.flush();
        return compile(syntaxTree, listener);
    }

    public QtfScript compile(final String fileName, final String text, final QtfListener listener) throws QtfCompilerException {
        return compile(fileName, text, listener, ProblemReporter.EARLY_EXIT);
    }

    public QtfScript compile(final String text, final QtfListener listener, final ProblemReporter problems) throws QtfCompilerException {
        return compile("<unknown>", text, listener, problems);
    }

    public QtfScript compile(final String text, final QtfListener listener) throws QtfCompilerException {
        return compile(text, listener, ProblemReporter.EARLY_EXIT);
    }

    public QtfScript compile(final SyntaxTree tree, final QtfListener listener) throws QtfCompilerException {
        try {
            if (classLoader == null) {
                classLoader = classLoaderFactory.get();
            }
            final String className = nameProvider.next();
            final QtfMemory memory = tree.context().createMemory(listener);
            final JvmClassComposer composer = tree.context().createClassComposer(className);
            final JvmRunnable runnable = JvmCompiler.compile(tree.flatten(), composer, className, classLoader);
            return new QtfScript(runnable, memory, tree.context().getInputs());
        } catch (final Exception e) {
            throw new QtfCompilerException(e);
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
