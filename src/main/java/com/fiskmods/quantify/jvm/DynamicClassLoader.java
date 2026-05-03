package com.fiskmods.quantify.jvm;

import org.jspecify.annotations.Nullable;

final class DynamicClassLoader extends ClassLoader {
    /// The ClassLoader to fall back on in the event that a class could neither be loaded nor found.
    private final @Nullable ClassLoader fallbackClassLoader;

    DynamicClassLoader(final @Nullable ClassLoader fallbackClassLoader) {
        /*
         * Ensure that dynamically generated classes can always be cast to
         * JvmRunnable, regardless of how it was loaded.
         */
        super(JvmRunnable.class.getClassLoader());
        this.fallbackClassLoader = fallbackClassLoader;
    }

    @Override
    public Class<?> loadClass(final String name) throws ClassNotFoundException {
        if (fallbackClassLoader == null) {
            return super.loadClass(name);
        }
        try {
            return super.loadClass(name);
        } catch (final ClassNotFoundException e) {
            return fallbackClassLoader.loadClass(name);
        }
    }

    Class<?> defineClass(final String name, final byte[] b) throws ClassFormatError {
        return defineClass(name, b, 0, b.length);
    }

    @Override
    public String toString() {
        return "QTF-JVM-BRIDGE";
    }
}
