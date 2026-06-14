package com.fiskmods.quantify.member;

public class FunctionScope extends Scope {
    private FunctionScope(final int level) {
        super(level);
    }

    public static FunctionScope create(final Scope inScope) {
        final FunctionScope scope = new FunctionScope(inScope.level + 1);
        scope.localIndexOffset = 0;
        scope.members.inheritAllExcept(inScope.members, MemberType.VARIABLE);
        return scope;
    }

    @Override
    public FunctionScope copy() {
        final FunctionScope scope = new FunctionScope(level + 1);
        scope.localIndexOffset = localIndexOffset;
        scope.members.inherit(members);
        return scope;
    }
}
