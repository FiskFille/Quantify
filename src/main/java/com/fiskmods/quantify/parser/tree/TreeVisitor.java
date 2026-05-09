package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.VarAddress;

public interface TreeVisitor {

    void visitAssignment(Assignment assign);

    void visitBlock(BlockTree block);

    void visitFunctionDef(FunctionDef func);

    void visitFunctionRef(FunctionRef func);

    void visitIfStatement(IfStatement ifStmt);

    void visitInterpolateStatement(InterpolateStatement lerp);

    void visitLerpAssignment(LerpAssignment assign);

    void visitNegatedValue(NegatedValue neg);

    void visitNumLiteral(NumLiteral lit);

    void visitOperation(Operation op);

    void visitReturn(Return ret);

    void visitVarAddress(VarAddress var);

    void visitVarDefinition(VarDefinitionTree var);

    default void visitTree(final Tree tree) {
        switch (tree) {
            case final Assignment t -> visitAssignment(t);
            case final BlockTree t -> visitBlock(t);
            case final FunctionDef t -> visitFunctionDef(t);
            case final FunctionRef t -> visitFunctionRef(t);
            case final IfStatement t -> visitIfStatement(t);
            case final InterpolateStatement t -> visitInterpolateStatement(t);
            case final LerpAssignment t -> visitLerpAssignment(t);
            case final NegatedValue t -> visitNegatedValue(t);
            case final NumLiteral t -> visitNumLiteral(t);
            case final Operation t -> visitOperation(t);
            case final Return t -> visitReturn(t);
            case final VarAddress t -> visitVarAddress(t);
            case final VarDefinitionTree t -> visitVarDefinition(t);
            default -> throw new IllegalStateException("Unexpected value: " + tree);
        }
    }
}
