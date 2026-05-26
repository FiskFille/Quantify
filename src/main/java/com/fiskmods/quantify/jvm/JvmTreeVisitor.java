package com.fiskmods.quantify.jvm;

import com.fiskmods.quantify.jvm.assignable.*;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.parser.tree.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.List;
import java.util.NoSuchElementException;

import static org.objectweb.asm.Opcodes.*;

public class JvmTreeVisitor implements TreeVisitor {
    private final String className;

    private final ClassVisitor cv;
    private final MethodVisitor mv;

    public JvmTreeVisitor(final String className, final ClassVisitor cv, final MethodVisitor mv) {
        this.className = className;
        this.cv = cv;
        this.mv = mv;
    }

    @Override
    public void visitAssignment(final Assignment assign) {
        if (assign.op() != null) {
            varVisitor(assign.targets()).visitModify(assign.value(), assign.op());
        } else {
            varVisitor(assign.targets()).visitSet(assign.value());
        }
    }

    @Override
    public void visitBlock(final BlockStatement block) {
        block.statements().forEach(this::visitStatement);
    }

    @Override
    public void visitConstDef(final ConstDefinitionTree cst) {
    }

    @Override
    public void visitExpressionStatement(final ExpressionStatement expStmt) {
        visitExpression(expStmt.expression());

        // Pop unused value from stack
        mv.visitInsn(POP2);
    }

    @Override
    public void visitFunctionDef(final FunctionDef func) {
        func.address().owner = className;

        final MethodVisitor mv = cv.visitMethod(ACC_STATIC | ACC_PUBLIC, func.address().name, func.address().descriptor, null, null);
        final TreeVisitor visitor = new JvmTreeVisitor(className, cv, mv);
        visitor.visitStatement(func.body());

        if (func.returnValue() == FunctionDef.ReturnValueType.MISSING) {
            mv.visitInsn(DCONST_0);
            mv.visitInsn(DRETURN);
        }
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    @Override
    public void visitFunctionRef(final FunctionRef func) {
        func.args().forEach(this::visitExpression);
        func.address().visit(mv, INVOKESTATIC, false);
    }

    @Override
    public void visitIdentifier(final Identifier identifier) {
    }

    @Override
    public void visitIfElse(final IfElseExpression ifElse) {
        if (ifElse.condition() instanceof final NumLiteral lit) {
            visitExpression(lit.value() > 0 ? ifElse.thenExpression() : ifElse.elseExpression());
            return;
        }

        final Label end = new Label();
        visitExpression(ifElse.condition());
        mv.visitInsn(D2I);

        final Label els = new Label();
        mv.visitJumpInsn(IFLE, els);
        visitExpression(ifElse.thenExpression());
        mv.visitJumpInsn(GOTO, end);
        mv.visitLabel(els);
        visitExpression(ifElse.elseExpression());
        mv.visitLabel(end);
    }

    @Override
    public void visitIfStatement(final IfStatement ifStmt) {
        if (ifStmt.condition() instanceof final NumLiteral lit) {
            if (lit.value() > 0) {
                visitStatement(ifStmt.body());
            }
            return;
        }

        final Label end = new Label();
        visitExpression(ifStmt.condition());
        mv.visitInsn(D2I);

        if (ifStmt.elseBody() == null) {
            mv.visitJumpInsn(IFLE, end);
            visitStatement(ifStmt.body());
        } else {
            final Label els = new Label();
            mv.visitJumpInsn(IFLE, els);
            visitStatement(ifStmt.body());
            mv.visitJumpInsn(GOTO, end);
            mv.visitLabel(els);
            visitStatement(ifStmt.elseBody());
        }
        mv.visitLabel(end);
    }

    @Override
    public void visitImport(final ImportStatement statement) {
    }

    @Override
    public void visitInput(final InputStatement input) {
        varVisitor(input.targetAddress()).visitSet(input.inputVar());
    }

    @Override
    public void visitInterpolateStatement(final InterpolateStatement lerp) {
        if (lerp.progress() instanceof final NumLiteral lit && lit.value() == 0) {
            return;
        }
        if (lerp.substitution() != null) {
            varVisitor(lerp.substitution()).visitSet(lerp.progress());
        }
        visitStatement(lerp.body());
    }

    @Override
    public void visitLerpAssignment(final LerpAssignment assign) {
        if (assign.progress() instanceof final NumLiteral lit) {
            if (lit.value() == 0) return;
            if (lit.value() == 1) {
                varVisitor(assign.targets()).visitSet(assign.value());
                return;
            }
        }

        // Interpolating towards 0 is the same as multiplying by (1-progress)
        if (!assign.rotational() && assign.value() instanceof final NumLiteral lit && lit.value() == 0) {
            varVisitor(assign.targets()).visitLerpToZero(assign.value(), assign.progress());
            return;
        }

        varVisitor(assign.targets()).visitLerp(assign.value(), assign.progress(), assign.rotational());
    }

    @Override
    public void visitMemberSelect(final MemberSelect sel) {
    }

    @Override
    public void visitNamespace(final NamespaceStatement namespace) {
        if (namespace.body() != null) {
            visitStatement(namespace.body());
        }
    }

    @Override
    public void visitNegatedValue(final NegatedExpression neg) {
        visitExpression(neg.expression());
        mv.visitInsn(DNEG);
    }

    @Override
    public void visitNumLiteral(final NumLiteral lit) {
        final double value = lit.value();
        if (value == 0) {
            mv.visitInsn(DCONST_0);
        } else if (value == 1) {
            mv.visitInsn(DCONST_1);
        } else {
            mv.visitLdcInsn(value);
        }
    }

    @Override
    public void visitOperation(final Operation op) {
        if (op.op() == Operator.POW && op.right() instanceof final NumLiteral lit
                && JvmUtil.optimizePow(mv, this, op.left(), lit.value())) {
            return;
        }

        visitExpression(op.left());
        visitExpression(op.right());
        op.op().apply(mv);
    }

    @Override
    public void visitParameter(final ParameterTree p) {
    }

    @Override
    public void visitReturnStatement(final ReturnStatement ret) {
        visitExpression(ret.expression());
        mv.visitInsn(DRETURN);
    }

    @Override
    public void visitVarDefinition(final VarDefinitionTree var) {
        if (var.initializer() != null) {
            varVisitor(var.targets()).visitSet(var.initializer());
            return;
        }

        // Public var storage needs no initialization
        if (!var.isPublic()) {
            varVisitor(var.targets()).visitInit();
        }
    }

    @Override
    public void visitVarRef(final VarRef var) {
        varVisitor(var.address()).visitGet();
        if (var.isNegated()) {
            mv.visitInsn(DNEG);
        }
    }

    public VarVisitor varVisitor(final VarAddress address) {
        return switch (address) {
            case LocalVar(final int id) -> new LocalVarVisitor(this, mv, id);
            case ArrayVar(final int id, final int arrayIndex) -> new ArrayVarVisitor(this, mv, id, arrayIndex);
            case final Struct.StructImpl struct -> new StructVarVisitor(mv, struct.index(), struct.size());
            default -> throw new IllegalStateException("Unexpected value: " + address);
        };
    }

    public VarVisitor varVisitor(final List<? extends VarRef> targets) {
        return switch (targets.size()) {
            case 0 -> throw new NoSuchElementException();
            case 1 -> varVisitor(targets.getFirst().address());
            default -> new VarVisitorList(this, targets);
        };
    }
}
