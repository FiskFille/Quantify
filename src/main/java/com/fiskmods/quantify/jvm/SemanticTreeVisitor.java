package com.fiskmods.quantify.jvm;

import com.fiskmods.quantify.Logger;
import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.lexer.Keywords;
import com.fiskmods.quantify.library.QtfLibrary;
import com.fiskmods.quantify.member.FunctionScope;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.member.Scope;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.tree.*;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.Optional;

public class SemanticTreeVisitor implements TreeVisitor {
    private final SyntaxContext context;
    private final ValidationStack stack;

    public SemanticTreeVisitor(final SyntaxContext context, final Logger logger) {
        this.context = context;
        stack = new ValidationStack(logger);
    }

    public boolean visit(final Iterable<? extends Statement> statements) {
        final int stackDepth = context.stackDepth();
        statements.forEach(this::visitStatement);

        final int currentStack = context.stackDepth();
        if (stackDepth != currentStack) {
            throw new IllegalStateException("Unbalanced stack: " + currentStack + ", expected: " + stackDepth);
        }
        return !stack.error;
    }

    @Override
    public void visitAssignment(final Assignment assign) {
        for (final VarRef target : assign.targets()) {
            visitVarRef(target);
            stack.pop(Type.DOUBLE_TYPE);
        }

        visitExpression(assign.value());
        stack.pop(Type.DOUBLE_TYPE);
    }

    @Override
    public void visitBlock(final BlockStatement block) {
        context.push(Scope::copy);
        block.statements().forEach(this::visitStatement);
        context.pop();
    }

    @Override
    public void visitConstDef(final ConstDefinitionTree cst) {
        final VarType<?> type = getType(cst.type());
        final double d;

        if (cst.value() instanceof final NumLiteral value) {
            visitNumLiteral(value);
            stack.pop(type);
            d = value.value();
        } else {
            stack.handle(new QtfException("constants can't be assigned to variables or functions"), cst.range());
            d = 0.0;
        }

        try {
            context.addMember(cst.name(), MemberType.CONSTANT, d);
        } catch (final QtfException e) {
            stack.handle(e, cst.range());
        }
    }

    @Override
    public void visitExpressionStatement(final ExpressionStatement expStmt) {
        visitExpression(expStmt.expression());
        stack.pop();
    }

    @Override
    public void visitFunctionDef(final FunctionDef func) {
        try {
            func.address = context.defineFunction(func.name(), func.parameters().size());
            context.addMember(func.name(), MemberType.FUNCTION, func.address);
        } catch (final QtfException e) {
            stack.handle(e, func.range());
        }

        context.push(FunctionScope::create);
        func.parameters().forEach(this::visitStatement);
        visitStatement(func.body());
        context.pop();
    }

    @Override
    public void visitFunctionRef(final FunctionRef func) {
        visitExpression(func.selector());
        final ExpressionValue parent = stack.pop();

        for (final Expression arg : func.args()) {
            visitExpression(arg);
            stack.pop(Type.DOUBLE_TYPE);
        }

        try {
            stack.push(parent.invoke(func));
        } catch (final QtfException e) {
            stack.handle(e, func.range());
        }
    }

    @Override
    public void visitIdentifier(final Identifier identifier) {
        final String name = identifier.name();
        if (Keywords.THIS.equals(name)) {
            stack.push(ExpressionValue._this(identifier, context));
        } else {
            stack.push(ExpressionValue.getMember(identifier, context, name));
        }
    }

    @Override
    public void visitIfElse(final IfElseExpression ifElse) {
        visitExpression(ifElse.condition());
        stack.pop(Type.DOUBLE_TYPE);

        visitExpression(ifElse.thenExpression());
        stack.pop(Type.DOUBLE_TYPE);

        visitExpression(ifElse.elseExpression());
        stack.pop(Type.DOUBLE_TYPE);

        stack.push(ExpressionValue.num(ifElse));
    }

    @Override
    public void visitIfStatement(final IfStatement ifStmt) {
        visitExpression(ifStmt.condition());
        stack.pop(Type.DOUBLE_TYPE);

        visitStatement(ifStmt.body());
        if (ifStmt.elseBody() != null) {
            visitStatement(ifStmt.elseBody());
        }
    }

    @Override
    public void visitImport(final ImportStatement statement) {
        if (context.scope().isInnerScope()) {
            stack.handle(new QtfException("import statements cannot be used in inner scopes"), statement.range());
        }

        try {
            final QtfLibrary library = context.libraries().getLibrary(statement.key());
            context.addMember(statement.name(), MemberType.LIBRARY, library);
        } catch (final QtfException e) {
            stack.handle(e, statement.range());
        }
    }

    @Override
    public void visitInput(final InputStatement input) {
        if (context.scope().isInnerScope()) {
            stack.handle(new QtfException("input statements cannot be used in inner scopes"), input.range());
        }

        try {
            input.inputAddress = context.addInputVariable(input.name(), input.index());
            input.targetAddress = context.scope().addLocalVariable(input.name());
        } catch (final QtfException e) {
            stack.handle(e, input.range());
        }
    }

    @Override
    public void visitInterpolateStatement(final InterpolateStatement lerp) {
        visitExpression(lerp.progress());
        stack.pop(Type.DOUBLE_TYPE);

        try {
            final Optional<VarAddress> var = context.scope().members.find(Keywords.INTERPOLATE, MemberType.VARIABLE);
            if (var.isPresent()) {
                lerp.progressAddress = var.get().cast(Keywords.INTERPOLATE, VarType.NUM);
            } else {
                lerp.progressAddress = context.scope().addLocalVariable(Keywords.INTERPOLATE);
            }
        } catch (final QtfException e) {
            stack.handle(e, lerp.range());
        }

        visitStatement(lerp.body());
    }

    @Override
    public void visitLerpAssignment(final LerpAssignment assign) {
        final Optional<VarAddress> var = context.scope().members.find(Keywords.INTERPOLATE, MemberType.VARIABLE);
        if (var.isPresent()) {
            assign.progress = var.get();
        } else {
            stack.handle(new QtfException("interpolation assignments can only be used inside interpolate blocks"), assign.range());
        }

        for (final VarRef target : assign.targets()) {
            visitVarRef(target);
            stack.pop(Type.DOUBLE_TYPE);
        }

        visitExpression(assign.value());
        stack.pop(Type.DOUBLE_TYPE);
    }

    @Override
    public void visitMemberSelect(final MemberSelect sel) {
        visitExpression(sel.expression());
        final ExpressionValue head = stack.peek();

        try {
            stack.push(stack.pop().select(sel, sel.identifier()));
        } catch (final QtfException e) {
            stack.handle(e, sel.expression().range());
            stack.push(head);
        }
    }

    @Override
    public void visitNamespace(final NamespaceStatement namespace) {
        visitExpression(namespace.expression());
        final ExpressionValue v = stack.pop();
        Namespace n;

        try {
            n = v.evaluateAsNamespace().fallback(context.getDefaultNamespace());
        } catch (final QtfException e) {
            n = null;
            stack.handle(e, namespace.expression().range());
        }

        if (n != null) {
            context.namespace().push(n);
            if (namespace.body() != null) {
                visitStatement(namespace.body());
                context.namespace().pop();
            }
        } else if (namespace.body() != null) {
            visitStatement(namespace.body());
        }
    }

    @Override
    public void visitNegatedValue(final NegatedExpression neg) {
        visitExpression(neg.expression());
    }

    @Override
    public void visitNumLiteral(final NumLiteral lit) {
        stack.push(ExpressionValue.num(lit));
    }

    @Override
    public void visitOperation(final Operation op) {
        visitExpression(op.left());
        stack.pop(Type.DOUBLE_TYPE);

        visitExpression(op.right());
        stack.pop(Type.DOUBLE_TYPE);

        stack.push(ExpressionValue.num(op));
    }

    @Override
    public void visitParameter(final ParameterTree p) {
        try {
            final VarType<?> type = getType(p.type());
            if (type != null) {
                if (p.type() != null && type != VarType.NUM) {
                    stack.handle(new QtfException("parameters only allow num type"), p.type().range());
                }
                type.defineLocal(p.name(), context.scope());
            }
        } catch (final QtfException e) {
            stack.handle(e, p.range());
        }
    }

    @Override
    public void visitReturnStatement(final ReturnStatement ret) {
        visitExpression(ret.expression());
        stack.pop();
    }

    private @Nullable VarType<?> getType(final @Nullable Identifier identifier) {
        if (identifier == null) {
            return VarType.NUM;
        }
        try {
            return VarType.getType(identifier.name());
        } catch (final QtfException e) {
            stack.handle(e, identifier.range());
            return null;
        }
    }

    @Override
    public void visitVarDefinition(final VarDefinitionTree var) {
        if (var.isPublic() && context.scope().isInnerScope()) {
            stack.handle(new QtfException("public vars cannot be defined in inner scopes"), var.range());
        }

        final VarType<?> type = getType(var.type());

        // Evaluate initializer before definition, avoids self-referential initializers
        if (var.initializer() != null) {
            visitExpression(var.initializer());
            stack.pop(type);
        }

        if (type == null) {
            return;
        }

        for (int i = 0; i < var.names().size(); i++) {
            final String name = var.names().get(i);
            try {
                var.targets[i] = var.isPublic() ? context.addPublicVar(name, type)
                        : type.defineLocal(name, context.scope());
            } catch (final QtfException e) {
                stack.handle(e, var.range());
            }
        }
    }

    @Override
    public void visitVarRef(final VarRef var) {
        visitExpression(var.expression());
    }
}
