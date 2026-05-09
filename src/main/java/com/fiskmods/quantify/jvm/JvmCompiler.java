package com.fiskmods.quantify.jvm;

import com.fiskmods.quantify.QtfCompiler;
import com.fiskmods.quantify.parser.tree.Statement;
import com.fiskmods.quantify.parser.tree.TreeVisitor;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

import static org.objectweb.asm.Opcodes.*;

public class JvmCompiler {
    public static final int CLASS_FILE_VERSION = 61;

    private static final String DEBUG_PATH = "debug";
    private static final String DEBUG_BYTECODE_PATH = DEBUG_PATH + "/bytecode";

    private static final String JVM_RUNNABLE = Type.getInternalName(JvmRunnable.class);

    private final DynamicClassLoader classLoader;
    private final Supplier<String> nextName;

    public JvmCompiler(final @Nullable ClassLoader fallbackClassLoader, final Supplier<String> nextName) {
        this.classLoader = new DynamicClassLoader(fallbackClassLoader);
        this.nextName = nextName;
    }

    public Class<?> compile(final List<? extends Statement> statements) {
        final String className = nextName.get();
        final String binaryName = className.replace('/', '.');

        final byte[] bytes = writeClass(className, statements);
        final Class<?> c = classLoader.defineClass(binaryName, bytes);

        if (QtfCompiler.DEBUG) {
            writeClassFile(c, bytes);
        }
        return c;
    }

    private byte[] writeClass(final String className, final List<? extends Statement> statements) {
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(CLASS_FILE_VERSION, ACC_PUBLIC | ACC_SUPER | ACC_FINAL, className, null, JVM_RUNNABLE, null);

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([D)V", null, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESPECIAL, JVM_RUNNABLE, "<init>", "([D)V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PROTECTED, "run", "([D[D)V", null, null);
        final TreeVisitor visitor = new JvmTreeVisitor(className, cw, mv);
        statements.forEach(visitor::visitTree);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }

    private void writeClassFile(final Class<?> c, final byte[] data) {
        try {
            final String packageName = c.getPackageName().replace('.', File.separatorChar);
            final String className = c.getSimpleName();

            File dir = new File(DEBUG_PATH, packageName);
            if (dir.exists() || dir.mkdirs()) {
                final Path p = dir.toPath().resolve(className + ".class");
                Files.write(p, data);
            }

            dir = new File(DEBUG_BYTECODE_PATH, packageName);
            if (dir.exists() || dir.mkdirs()) {
                final Path p = dir.toPath().resolve(className + ".txt");

                try (final OutputStream out = Files.newOutputStream(p)) {
                    final ClassReader reader = new ClassReader(data);
                    final TraceClassVisitor tcv = new TraceClassVisitor(new PrintWriter(out));
                    reader.accept(tcv, 0);
                    out.flush();
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
