package com.fiskmods.quantify.parser.tree;

public interface TreeVisitor {

    void visitAssignment(Assignment assign);

    void visitBlock(BlockStatement block);

    void visitCompoundAssignment(CompoundAssignment assign);

    void visitConstDef(ConstDefinitionTree cst);

    void visitExpressionStatement(ExpressionStatement expStmt);

    void visitFunctionDef(FunctionDef func);

    void visitFunctionRef(FunctionRef func);

    void visitIdentifier(Identifier identifier);

    void visitIfElse(IfElseExpression ifElse);

    void visitIfStatement(IfStatement ifStmt);

    void visitImport(ImportStatement statement);

    void visitInput(InputStatement input);

    void visitInterpolateStatement(InterpolateStatement lerp);

    void visitLerpAssignment(LerpAssignment assign);

    void visitMemberSelect(MemberSelect sel);

    void visitNamespace(NamespaceStatement namespace);

    void visitNegatedValue(NegatedExpression neg);

    void visitNumLiteral(NumLiteral lit);

    void visitOperation(Operation op);

    void visitParameter(ParameterTree p);

    void visitReturnStatement(ReturnStatement ret);

    void visitVarDefinition(VarDefinitionTree var);

    void visitVarRef(VarRef var);

    default void visitExpression(final Expression expression) {
        switch (expression) {
            case final FunctionRef t -> visitFunctionRef(t);
            case final IfElseExpression t -> visitIfElse(t);
            case final Identifier t -> visitIdentifier(t);
            case final MemberSelect t -> visitMemberSelect(t);
            case final NegatedExpression t -> visitNegatedValue(t);
            case final NumLiteral t -> visitNumLiteral(t);
            case final Operation t -> visitOperation(t);
            case final VarRef t -> visitVarRef(t);
            default -> throw new IllegalStateException("Unexpected value: " + expression);
        }
    }

    default void visitStatement(final Statement statement) {
        switch (statement) {
            case final Assignment t -> visitAssignment(t);
            case final BlockStatement t -> visitBlock(t);
            case final CompoundAssignment t -> visitCompoundAssignment(t);
            case final ConstDefinitionTree t -> visitConstDef(t);
            case final ExpressionStatement t -> visitExpressionStatement(t);
            case final FunctionDef t -> visitFunctionDef(t);
            case final IfStatement t -> visitIfStatement(t);
            case final ImportStatement t -> visitImport(t);
            case final InputStatement t -> visitInput(t);
            case final InterpolateStatement t -> visitInterpolateStatement(t);
            case final LerpAssignment t -> visitLerpAssignment(t);
            case final NamespaceStatement t -> visitNamespace(t);
            case final ParameterTree t -> visitParameter(t);
            case final ReturnStatement t -> visitReturnStatement(t);
            case final VarDefinitionTree t -> visitVarDefinition(t);
            default -> throw new IllegalStateException("Unexpected value: " + statement);
        }
    }
}
