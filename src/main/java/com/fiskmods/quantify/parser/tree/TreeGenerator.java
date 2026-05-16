package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.Token;
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

    public FunctionDef newFunction(final String name, final boolean isVisible, final FunctionDef.DefinedFunctionAddress address, final Statement body, final FunctionDef.ReturnValueType returnValue) {
        final var tree = new FunctionDef(name, isVisible, address, body, returnValue);
        tree.range = finishTree();
        return tree;
    }

    public FunctionRef newFunctionRef(final FunctionAddress address, final List<? extends Expression> args) {
        return FunctionRef.of(address, args, finishTree());
    }

    public IfStatement newIfStatement(final Expression condition, final Statement body, final @Nullable Statement elseBody) {
        final var tree = new IfStatement(condition, body, elseBody);
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

    public NumLiteral newNumLiteral(final double value) {
        return newNumLiteral(value, finishTree());
    }

    public NumLiteral newNumLiteral(final double value, final Token.Range range) {
        final var tree = new NumLiteral(value);
        tree.range = range;
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

    public VarRef newVariableRef(final VarAddress address, final boolean isNegated, final Token.Range range) {
        final var tree = new VarRef(address, isNegated);
        tree.range = range;
        return tree;
    }
}
