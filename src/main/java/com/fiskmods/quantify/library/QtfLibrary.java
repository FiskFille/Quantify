package com.fiskmods.quantify.library;

import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.member.SimpleNamespace;

import java.util.function.UnaryOperator;

public record QtfLibrary(
        String key,
        Namespace namespace
) {
    public static QtfLibrary create(String name, UnaryOperator<SimpleNamespace.Builder> namespace) {
        var builder = namespace.apply(SimpleNamespace.builder(name));
        return new QtfLibrary(name, builder.build());
    }
}
