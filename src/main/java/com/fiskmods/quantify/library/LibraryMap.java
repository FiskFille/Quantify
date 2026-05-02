package com.fiskmods.quantify.library;

import com.fiskmods.quantify.exception.QtfException;

import java.util.HashMap;
import java.util.Map;

public final class LibraryMap {
    public static final LibraryMap NONE = new LibraryMap(Map.of());

    private final Map<String, QtfLibrary> libraries;

    private LibraryMap(final Map<String, QtfLibrary> libraries) {
        this.libraries = libraries;
    }

    public QtfLibrary getLibrary(final String key) throws QtfException {
        final QtfLibrary library = libraries.get(key);
        if (library != null) {
            return library;
        }
        throw new QtfException("Unknown library '%s'".formatted(key));
    }

    public static LibraryMap of(final QtfLibrary library) {
        return new LibraryMap(
                Map.of(library.key(), library)
        );
    }

    public static LibraryMap of(final QtfLibrary... libraries) {
        final Builder builder = builder();
        for (final QtfLibrary library : libraries) {
            builder.addLibrary(library);
        }
        return builder.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Map<String, QtfLibrary> libraries = new HashMap<>();
        private Builder() {}

        public Builder addLibrary(final QtfLibrary library) {
            final QtfLibrary prev = libraries.put(library.key(), library);
            if (prev != null) {
                throw new IllegalArgumentException("Library already added: " + library.key());
            }
            return this;
        }

        public LibraryMap build() {
            return new LibraryMap(Map.copyOf(libraries));
        }
    }
}
