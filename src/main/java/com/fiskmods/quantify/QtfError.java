package com.fiskmods.quantify;

import com.fiskmods.quantify.lexer.TextScanner;
import org.jspecify.annotations.Nullable;

import javax.tools.Diagnostic;
import java.util.Locale;

public class QtfError implements Diagnostic<QtfSourceFile> {
    private final QtfSourceFile sourceFile;
    private final TextScanner.Location location;

    private final String message;

    public QtfError(final QtfSourceFile sourceFile, final TextScanner.Location location, final String message) {
        this.sourceFile = sourceFile;
        this.location = location;
        this.message = message;
    }

    @Override
    public Kind getKind() {
        return Kind.ERROR;
    }

    @Override
    public QtfSourceFile getSource() {
        return sourceFile;
    }

    @Override
    public long getPosition() {
        return location.lineStart() + location.column() - 1;
    }

    @Override
    public long getStartPosition() {
        return location.lineStart();
    }

    @Override
    public long getEndPosition() {
        return location.lineEnd();
    }

    @Override
    public long getLineNumber() {
        return location.line();
    }

    @Override
    public long getColumnNumber() {
        return location.column();
    }

    @Override
    public @Nullable String getCode() {
        return null;
    }

    @Override
    public String getMessage(final Locale locale) {
        return message;
    }
}
