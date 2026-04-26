package com.fiskmods.quantify.exception;

import com.fiskmods.quantify.library.QtfLibrary;
import com.fiskmods.quantify.member.MemberType;

public final class QtfErrors {
    private QtfErrors() {}

    public static QtfException undefined(MemberType<?> expectedType, String name) {
        return new QtfException("Undefined %s '%s'".formatted(expectedType.name(), name));
    }

    public static QtfException undefined(MemberType<?> expectedType, String name, String parentName) {
        return new QtfException("Undefined %s '%s' in %s".formatted(expectedType.name(), name, parentName));
    }

    public static QtfException undefined(MemberType<?> expectedType, String name, QtfLibrary library) {
        return undefined(expectedType, name, library.key());
    }
}
