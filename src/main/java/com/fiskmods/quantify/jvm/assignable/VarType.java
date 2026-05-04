package com.fiskmods.quantify.jvm.assignable;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.member.Scope;

public record VarType<T extends VarAddress>(String typeName, int size, boolean isAssignable) {

    public static final VarType<VarAddress> NUM = new VarType<>("num", 2, true);
    public static final VarType<Struct> STRUCT = new VarType<>("struct", 1, false);

    public static final VarType<?>[] TYPES = {NUM, STRUCT};

    @SuppressWarnings("unchecked")
    public T defineLocal(final String name, final Scope scope) throws QtfException {
        if (this == VarType.NUM) {
            return (T) scope.addLocalVariable(name);
        } else if (this == STRUCT) {
            return (T) scope.addStruct(name);
        }
        throw new QtfException("Unable to define %s '%s'".formatted(typeName, name));
    }

    public static VarType<?> getType(final String key) throws QtfException {
        for (final VarType<?> type : TYPES) {
            if (type.typeName.equals(key)) {
                return type;
            }
        }
        throw new QtfException("Unknown variable type '%s'".formatted(key));
    }
}
