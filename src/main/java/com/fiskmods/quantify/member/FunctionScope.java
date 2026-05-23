package com.fiskmods.quantify.member;

import com.fiskmods.quantify.exception.QtfException;

public class FunctionScope extends Scope {
    private boolean hasReturnValue;

    private FunctionScope(final Namespace namespace, final int level) {
        super(namespace, level);
    }

    public static FunctionScope create(final Scope inScope) throws QtfException {
        final FunctionScope scope = new FunctionScope(inScope.namespace, inScope.level + 1);
        scope.lerpProgress = inScope.lerpProgress;
        scope.localIndexOffset = 0;
        scope.members.inheritAllExcept(inScope.members, MemberType.VARIABLE);
        return scope;
    }

    @Override
    public FunctionScope copy(final Namespace namespace) {
        final FunctionScope scope = new FunctionScope(namespace, level + 1);
        scope.lerpProgress = lerpProgress;
        scope.localIndexOffset = localIndexOffset;
        scope.members.inherit(members);
        return scope;
    }

    public void setHasReturnValue(final boolean hasReturnValue) {
        this.hasReturnValue = hasReturnValue;
    }

    public boolean hasReturnValue() {
        return hasReturnValue;
    }
}
