package com.fiskmods.quantify.member;

import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.library.QtfLibrary;

import java.util.function.Predicate;

public record MemberType<T>(String name, boolean isLocal) implements Predicate<MemberMap.Member<?>> {
    public static final MemberType<VarAddress> VARIABLE = new MemberType<>("variable", true);
    public static final MemberType<Double> CONSTANT = new MemberType<>("constant", true);
    public static final MemberType<FunctionAddress> FUNCTION = new MemberType<>("function", true);

    public static final MemberType<QtfLibrary> LIBRARY = new MemberType<>("library", false);

    public Scope scope(final ScopeProvider provider) {
        return isLocal ? provider.scope() : provider.global();
    }

    @Override
    public boolean test(final MemberMap.Member<?> member) {
        return member.type() == this;
    }
}
