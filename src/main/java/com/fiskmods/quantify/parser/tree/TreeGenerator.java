package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.library.QtfLibrary;
import com.fiskmods.quantify.member.Namespace;
import org.jspecify.annotations.Nullable;

import java.util.List;

public abstract class TreeGenerator {

    public abstract void startTree();

    protected abstract Token.Range finishTree();

    public Assignment newAssignment(final List<? extends VarRef> targets, final Expression value, final @Nullable Operator op) {
        final var tree = new Assignment(targets, value, op);
        tree.range = finishTree();
        return tree;
    }

    public BlockStatement newBlockStatement(final List<? extends Statement> statements) {
        final var tree = new BlockStatement(statements);
        tree.range = finishTree();
        return tree;
    }

    public ConstDefinitionTree newConst(final Identifier name, final VarType<?> type, final Expression value) {
        final var tree = new ConstDefinitionTree(name, type, value);
        tree.range = finishTree();
        return tree;
    }

    public FunctionDef newFunction(final Identifier name, final List<ParameterTree> parameters, final Statement body, final FunctionDef.ReturnValueType returnValue, final FunctionDef.DefinedFunctionAddress address) {
        final var tree = new FunctionDef(name, parameters, body, returnValue, address);
        tree.range = finishTree();
        return tree;
    }

    public FunctionRef newFunctionRef(final Expression selector, final FunctionAddress address, final List<? extends Expression> args) {
        return FunctionRef.of(selector, address, args, finishTree());
    }

    public IfStatement newIfStatement(final Expression condition, final Statement body, final @Nullable Statement elseBody) {
        final var tree = new IfStatement(condition, body, elseBody);
        tree.range = finishTree();
        return tree;
    }

    public IfElseExpression newIfElse(final Expression condition, final Expression thenExpression, final Expression elseExpression) {
        final var tree = new IfElseExpression(condition, thenExpression, elseExpression);
        tree.range = finishTree();
        return tree;
    }

    public ImportStatement newImportStatement(final Identifier name, final String key, final QtfLibrary library) {
        final var tree = new ImportStatement(name, key, library);
        tree.range = finishTree();
        return tree;
    }

    public InputStatement newInput(final int index, final Identifier name, final VarAddress inputAddress, final VarAddress targetAddress) {
        final var tree = new InputStatement(index, name, inputAddress, targetAddress);
        tree.range = finishTree();
        return tree;
    }

    public InterpolateStatement newInterpolateStatement(final Expression progress, final @Nullable VarAddress substitution, final Statement body) {
        final var tree = new InterpolateStatement(progress, substitution, body);
        tree.range = finishTree();
        return tree;
    }

    public LerpAssignment newLerpAssignment(final List<? extends VarRef> targets, final Expression value, final Expression progress, final boolean rotational) {
        final var tree = new LerpAssignment(targets, value, progress, rotational);
        tree.range = finishTree();
        return tree;
    }

    public NamespaceStatement newNamespaceStatement(final Expression expression, final Namespace namespace, final @Nullable Statement body) {
        final var tree = new NamespaceStatement(expression, namespace, body);
        tree.range = finishTree();
        return tree;
    }

    public NumLiteral newNumLiteral(final double value) {
        return newNumLiteral(value, finishTree());
    }

    public NumLiteral newNumLiteral(final double value, final Token.Range range) {
        final var tree = new NumLiteral(value);
        tree.range = range;
        return tree;
    }

    public ParameterTree newParameter(final Identifier name, final VarType<?> type, final VarAddress address) {
        final var tree = new ParameterTree(name, type, address);
        tree.range = finishTree();
        return tree;
    }

    public ReturnStatement newReturnStatement(final Expression expression) {
        final var tree = new ReturnStatement(expression);
        tree.range = finishTree();
        return tree;
    }

    public ReturnStatement newImplicitReturnStatement(final Expression expression) {
        final var tree = new ReturnStatement(expression);
        tree.range = expression.range;
        return tree;
    }

    public VarDefinitionTree newVariable(final List<? extends VarRef> targets, final VarType<?> type, final @Nullable Expression initializer, final boolean isPublic) {
        final var tree = new VarDefinitionTree(targets, type, initializer, isPublic);
        tree.range = finishTree();
        return tree;
    }

    public VarRef newVariableRef(final Expression expression, final VarAddress address, final boolean isNegated) {
        final var tree = new VarRef(expression, address, isNegated);
        tree.range = expression.range;
        return tree;
    }

    public MemberSelect newMemberSelect(final Expression expression, final String identifier) {
        final var tree = new MemberSelect(expression, identifier);
        tree.range = finishTree();
        return tree;
    }
}
