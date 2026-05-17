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
        block.statements().forEach(this::visitTree);
    }

    @Override
    public void visitExpressionStatement(final ExpressionStatement expStmt) {
        visitTree(expStmt.expression());

        // Pop unused value from stack
        mv.visitInsn(POP2);
    }

    @Override
    public void visitFunctionDef(final FunctionDef func) {
        func.address().owner = className;

        final MethodVisitor mv = cv.visitMethod(ACC_STATIC | ACC_PUBLIC, func.address().name, func.address().descriptor, null, null);
        final TreeVisitor visitor = new JvmTreeVisitor(className, cv, mv);
        visitor.visitTree(func.body());

        if (func.returnValue() == FunctionDef.ReturnValueType.MISSING) {
            mv.visitInsn(DCONST_0);
            mv.visitInsn(DRETURN);
        }
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    @Override
    public void visitFunctionRef(final FunctionRef func) {
        func.args().forEach(this::visitTree);
        func.address().visit(mv, INVOKESTATIC, false);
    }

    @Override
    public void visitIfStatement(final IfStatement ifStmt) {
        if (ifStmt.condition() instanceof final NumLiteral lit) {
            if (lit.value() > 0) {
                visitTree(ifStmt.body());
            }
            return;
        }

        final Label end = new Label();
        visitTree(ifStmt.condition());
        mv.visitInsn(D2I);

        if (ifStmt.elseBody() == null) {
            mv.visitJumpInsn(IFLE, end);
            visitTree(ifStmt.body());
        } else {
            final Label els = new Label();
            mv.visitJumpInsn(IFLE, els);
            visitTree(ifStmt.body());
            mv.visitJumpInsn(GOTO, end);
            mv.visitLabel(els);
            visitTree(ifStmt.elseBody());
        }
        mv.visitLabel(end);
    }

    @Override
    public void visitInterpolateStatement(final InterpolateStatement lerp) {
        if (lerp.progress() instanceof final NumLiteral lit && lit.value() == 0) {
            return;
        }
        if (lerp.substitution() != null) {
            varVisitor(lerp.substitution()).visitSet(lerp.progress());
        }
        visitTree(lerp.body());
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
    public void visitNegatedValue(final NegatedExpression neg) {
        visitTree(neg.expression());
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

        visitTree(op.left());
        visitTree(op.right());
        op.op().apply(mv);
    }

    @Override
    public void visitReturnStatement(final ReturnStatement ret) {
        visitTree(ret.expression());
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
