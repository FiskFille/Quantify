package com.fiskmods.quantify.member;

import com.fiskmods.quantify.exception.QtfException;

public class FunctionScope extends Scope {
    private final String[] parameters;

    protected boolean hasReturnValue;

    private FunctionScope(final Namespace namespace, final String[] parameters, final int level) {
        super(namespace, level);
        this.parameters = parameters;
    }

    public static FunctionScope create(final Scope inScope, final String[] parameters) throws QtfException {
        final FunctionScope scope = new FunctionScope(inScope.namespace, parameters, 0);
        scope.lerpProgress = inScope.lerpProgress;
        scope.localIndexOffset = 0;
        scope.members.inheritAllExcept(inScope.members, MemberType.VARIABLE);

        for (final String param : parameters) {
            scope.addLocalVariable(param);
        }
        return scope;
    }

    @Override
    public FunctionScope copy(final Namespace namespace) {
        final FunctionScope scope = new FunctionScope(namespace, parameters, level + 1);
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

    @Override
    public boolean isInnerScope() {
        return true;
    }
}
