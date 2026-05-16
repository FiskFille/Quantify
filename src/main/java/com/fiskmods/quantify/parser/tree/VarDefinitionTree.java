package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.assignable.VarType;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class VarDefinitionTree extends Tree implements Statement {
    private final List<? extends VarRef> targets;
    private final VarType<?> type;
    private final @Nullable Expression initializer;
    private final boolean isPublic;

    public VarDefinitionTree(final List<? extends VarRef> targets, final VarType<?> type, final @Nullable Expression initializer, final boolean isPublic) {
        this.targets = targets;
        this.type = type;
        this.initializer = initializer;
        this.isPublic = isPublic;
    }

    public List<? extends VarRef> targets() {
        return targets;
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
