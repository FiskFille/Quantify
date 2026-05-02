package com.fiskmods.quantify;

import javax.tools.FileObject;
import java.io.*;
import java.net.URI;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Path;

public class QtfSourceFile implements FileObject {
    protected final URI uri;

    public QtfSourceFile(final URI uri) {
        if (uri.getPath() == null) {
            throw new IllegalArgumentException("URI must have a path: " + uri);
        }
        this.uri = uri;
    }

    public static QtfSourceFile of(final Path path) {
        return new QtfSourceFile(path.toUri()) {
            @Override
            public String getName() {
                return path.toString();
            }
        };
    }

    public static QtfSourceFile create(final String name, final CharSequence charContent) {
        return new InMemory(name, charContent);
    }

    @Override
    public URI toUri() {
        return uri;
    }

    @Override
    public String getName() {
        return toUri().getPath();
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return toUri().toURL().openStream();
    }

    @Override
    public OutputStream openOutputStream() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reader openReader(final boolean ignoreEncodingErrors) throws IOException {
        return new InputStreamReader(openInputStream(), Charset.defaultCharset());
    }

    @Override
    public CharSequence getCharContent(final boolean ignoreEncodingErrors) throws IOException {
        try (final BufferedReader reader = new BufferedReader(openReader(ignoreEncodingErrors))) {
            final StringBuilder s = new StringBuilder();
            final char[] buf = new char[1024];
            int n;
            while ((n = reader.read(buf, 0, buf.length)) != -1) {
                s.append(buf, 0, n);
            }
            return s;
        }
    }

    @Override
    public Writer openWriter() {
        return new OutputStreamWriter(openOutputStream());
    }

    @Override
    public long getLastModified() {
        return 0L;
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[" + toUri() + "]";
    }

    private static class InMemory extends QtfSourceFile {
        private final String name;
        private final CharSequence charContent;

        public InMemory(final String name, final CharSequence charContent) {
            super(URI.create("string:///" + name.replace('.', '/') + ".qtf"));
            this.name = name;
            this.charContent = charContent;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
            return charContent;
        }

        @Override
        public Reader openReader(final boolean ignoreEncodingErrors) {
            if (charContent instanceof final CharBuffer buffer && buffer.hasArray()) {
                return new CharArrayReader(buffer.array());
            } else {
                return new StringReader(charContent.toString());
            }
        }

        @Override
        public InputStream openInputStream() {
            throw new UnsupportedOperationException();
        }
    }
}
