package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.VarType;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class VarDefinitionTree extends Statement {
    private final List<String> names;
    private final VarType<?> type;
    private final @Nullable Expression initializer;
    private final boolean isPublic;

    public final VarAddress[] targets;

    VarDefinitionTree(final List<String> names, final VarType<?> type, final @Nullable Expression initializer, final boolean isPublic) {
        this.names = names;
        this.type = type;
        this.initializer = initializer;
        this.isPublic = isPublic;
        this.targets = new VarAddress[names.size()];
    }

    public List<String> names() {
        return names;
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
