package com.fiskmods.quantify.jvm.assignable;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.member.Scope;
import com.fiskmods.quantify.parser.element.Assignable;
import com.fiskmods.quantify.parser.element.Value;

public record VarType<T extends Value & Assignable>(String typeName, int size, boolean isAssignable) {
    public static final VarType<NumVar> NUM = new VarType<>("num", 2, true);
    public static final VarType<Struct> STRUCT = new VarType<>("struct", 1, false);

    public static final VarType<?>[] TYPES = {NUM, STRUCT};

    @SuppressWarnings("unchecked")
    public VarAddress<T> defineLocal(String name, Scope scope) throws QtfException {
        if (this == VarType.NUM) {
            return (VarAddress<T>) scope.addLocalVariable(name);
        } else if (this == STRUCT) {
            return (VarAddress<T>) scope.addStruct(name);
        }
        throw new QtfException("Unable to define %s '%s'"
                .formatted(typeName, name));
    }

    public static VarType<?> getType(String key) throws QtfException {
        for (VarType<?> type : TYPES) {
            if (type.typeName.equals(key)) {
                return type;
            }
        }
        throw new QtfException("Unknown variable type '%s'".formatted(key));
    }
}
