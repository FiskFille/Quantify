package com.fiskmods.quantify.member;

import java.util.function.Predicate;
import java.util.stream.Stream;

@FunctionalInterface
public interface QtfListener {
    void listen(Resolver resolver, Output output);

    @FunctionalInterface
    interface Resolver {
        /**
         * <p>Resolves the variable against the provided name.</p>
         * <p>Names prefixed with <code>.</code> are output variables</p>
         */
        void subscribe(Variable var, String name);

        default void subscribe(final OutputTree tree, final Output output) {
            tree.resolve(this, output);
        }
    }

    @FunctionalInterface
    interface Output {
        Stream<String> keys();

        default Output filter(final Predicate<String> predicate) {
            return () -> keys().filter(predicate);
        }
    }
}
