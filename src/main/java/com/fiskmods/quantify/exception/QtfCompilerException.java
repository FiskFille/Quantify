package com.fiskmods.quantify.exception;

import com.fiskmods.quantify.QtfSourceFile;
import org.jspecify.annotations.Nullable;

public class QtfCompilerException extends QtfException {
    private final @Nullable QtfSourceFile sourceFile;

    public QtfCompilerException(final String message, final @Nullable QtfSourceFile sourceFile) {
        super(message);
        this.sourceFile = sourceFile;
    }

    public QtfCompilerException(final Throwable cause, final @Nullable QtfSourceFile sourceFile) {
        super(cause);
        this.sourceFile = sourceFile;
    }

    public QtfCompilerException(final String message, final Throwable cause, final @Nullable QtfSourceFile sourceFile) {
        super(message, cause);
        this.sourceFile = sourceFile;
    }

    public QtfCompilerException(final String message) {
        this(message, null);
    }

    public QtfCompilerException(final Throwable cause) {
        this(cause, null);
    }

    public static QtfCompilerException attachSource(final QtfCompilerException e, final QtfSourceFile sourceFile) {
        return new QtfCompilerException(e.getMessage(), e.getCause(), sourceFile);
    }

    public @Nullable QtfSourceFile getSource() {
        return sourceFile;
    }
}
