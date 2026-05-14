package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.assignable.VarType;
import org.jspecify.annotations.Nullable;

public final class VarDefinitionTree extends Tree implements Statement {
    private final Assignable target;
    private final VarType<?> type;
    private final @Nullable Expression initializer;
    private final boolean isPublic;

    public VarDefinitionTree(final Assignable target, final VarType<?> type, final @Nullable Expression initializer, final boolean isPublic) {
        this.target = target;
        this.type = type;
        this.initializer = initializer;
        this.isPublic = isPublic;
    }

    public Assignable target() {
        return target;
    }

    public VarType<?> type() {
        return type;
    }

    public @Nullable Expression initializer() {
        return initializer;
    }

    public boolean isPublic() {
        return isPublic;
    }
}
